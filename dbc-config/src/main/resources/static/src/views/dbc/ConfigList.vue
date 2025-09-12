<script lang="ts" setup xmlns="http://www.w3.org/1999/html">
import {
  Edit, ArrowLeft, EditPen, Delete, Search, DocumentCopy, Plus
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessageBox} from 'element-plus'
import {FormInstance, FormRules, TableInstance} from 'element-plus'
import router from "@/router";
import {getLangData} from "@/i18n/locale";

const langData = getLangData()
const dataType = ref([])
onMounted(() => {
  queryConfigList()
  queryInitial()
});

const queryInitial = () => {
  axios({
    url: '/config/initial',
    method: 'get',
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      dataType.value = res.data.body.dataType || []
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const goConfigProfile = () => {
  let query = router.currentRoute.value.query
  delete query.profileName
  delete query.profileDesc
  router.push({
    path: '/config/profile',
    query: query
  })
}

const form = reactive({
  pageNum: 1,
  pageSize: 10,
  configKey: '',
  configValue: ''
})
const data = reactive({
  total: 0,
  configList: []
})

const queryConfigList = () => {
  axios({
    url: '/config/list',
    method: 'post',
    data: {
      asp: router.currentRoute.value.query,
      config: form
    },
    params: {
      pageNum: form.pageNum,
      pageSize: form.pageSize
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.total = res.data.body.total
      data.configList = res.data.body.list
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref(langData.dialogTitleAdd)
const configForm = reactive({
  configKey: '',
  configValue: '',
  dataType: 'java.lang.String',
})
const showConfigAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = langData.dialogTitleAdd
  configForm.configKey = ''
  configForm.configValue = ''
  configForm.dataType='java.lang.String'
}

const showConfigEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = langData.dialogTitleEdit
  configForm.configKey = scope.row.configKey
  configForm.configValue = scope.row.configValue
  configForm.dataType=scope.row.dataType
}

const configFormRef = ref<FormInstance>();
const configRules = reactive<FormRules>({
  configKey: [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}],
  configValue: [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}]
})

const updateConfig = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/config',
        method: dialogTitle.value == langData.dialogTitleAdd ? 'post' : 'put',
        data: {
          asp: router.currentRoute.value.query,
          config: configForm
        },
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, "success")
          dialogFormVisible.value = false
          queryConfigList()
        } else {
          let content = res.config.baseURL + res.config.url + ': ' + res.data.errorMessage;
          msg(content, "warning")
        }
      }).catch((err: any) => {
        console.log('', err)
        msg(err?.response.data.errorMessage, 'error')
      })
    }
  })
}

const deleteConfig = (scope) => {
  axios({
    url: '/config',
    method: 'delete',
    data: {
      asp: router.currentRoute.value.query,
      config: {configKey: scope.row.configKey}
    },
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryConfigList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const multipleTableRef = ref<TableInstance>()
const selectAll = ref(false)
const deleteMultipleConfig = () => {
  let selectionRows = multipleTableRef.value?.getSelectionRows()
  if (!selectionRows || selectionRows.length == 0) {
    ElMessageBox.alert(langData.configListMsgBoxAlert, langData.msgBoxTitle, {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  let configKeys = selectionRows.map(r => r.configKey);
  axios({
    url: '/config/multiple',
    method: 'delete',
    data: {
      asp: router.currentRoute.value.query,
      configKeys: configKeys
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryConfigList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const goConfigQuery = (scope) => {
  let query = scope.row;
  query.fromPage = 'configList'
  router.push({path: '/config/query', query: query})
}

const backupConfig = () => {
  axios({
    url: '/config/backup',
    method: 'post',
    data: router.currentRoute.value.query
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const debounce = (callback: (...args: any[]) => void, delay: number) => {
  let tid: any;
  return function (...args: any[]) {
    const ctx = self;
    tid && clearTimeout(tid);
    tid = setTimeout(() => {
      callback.apply(ctx, args);
    }, delay);
  };
};

const _ = (window as any).ResizeObserver;
(window as any).ResizeObserver = class ResizeObserver extends _ {
  constructor(callback: (...args: any[]) => void) {
    callback = debounce(callback, 20);
    super(callback);
  }
};

</script>

<template>
  <div class="container">
    <el-page-header :icon="ArrowLeft" @back="goConfigProfile">
      <template #content>
        <span class="text-large font-600 mr-3" style="font-size: 15px"> {{
            langData.configProfileHeaderCreator
          }}：{{ router.currentRoute.value.query.username }}，{{ langData.configProfileHeaderServiceName }}: {{
            router.currentRoute.value.query.serviceName
          }}，{{ langData.configProfileProfileName }}: {{
            router.currentRoute.value.query.profileName
          }}({{ router.currentRoute.value.query.profileDesc }}) </span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0"></el-divider>

    <el-form :model="form" size="small" label-position="right" inline-message inline>
      <el-form-item :label="langData.configListTableConfigKey" prop="configKey">
        <el-input v-model="form.configKey" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item :label="langData.configListTableConfigValue" prop="configValue">
        <el-input v-model="form.configValue" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryConfigList()" :icon="Search">{{ langData.btnSearch }}
        </el-button>
        <el-popconfirm :title="langData.confirmDelete" confirm-button-type="danger" @confirm="deleteMultipleConfig()">
          <template #reference>
            <el-button type="danger" :icon="Delete">{{ langData.configListTableBatchDelete }}</el-button>
          </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table ref="multipleTableRef" :select-on-indeterminate="selectAll" :data="data.configList" style="width: 100%"
              table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column type="selection" header-align="center" align="center"/>
      <el-table-column fixed="left" :label="langData.tableHeaderOp" width="120" header-align="center" align="center">
        <template #default="scope">
          <el-icon @click="showConfigEditDialog(scope)" color="#3F9EFF" style="cursor: pointer; margin-left: 10px" :size="14"><Edit/></el-icon>
          <el-popconfirm :title="langData.confirmDelete" @confirm="deleteConfig(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-icon color="red" style="cursor: pointer; margin-left: 10px" :size="14"><Delete/></el-icon>
            </template>
          </el-popconfirm>
          <el-tooltip :content="langData.configListTableBatchManage" effect="dark" placement="top">
            <el-icon @click="goConfigQuery(scope)" color="#FFB85C" style="cursor: pointer; margin-left: 10px" :size="14"><DocumentCopy/></el-icon>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="configKey" :label="langData.configListTableConfigKey" :show-overflow-tooltip="true"
                       header-align="center"
                       align="left"/>
      <el-table-column prop="configValue" :label="langData.configListTableConfigValue" :show-overflow-tooltip="true"
                       header-align="center"
                       align="left"/>
      <el-table-column prop="fmtCreatedAt" :label="langData.tableHeaderCreateTime" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="180"/>
      <el-table-column prop="fmtUpdatedAt" :label="langData.tableHeaderUpdateTime" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="180"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                   @size-change="queryConfigList()"
                   @current-change="queryConfigList()" @prev-click="queryConfigList()" @next-click="queryConfigList()"
                   :small="true" :background="true"
                   :page-sizes="[5, 10, 20, 50, 100,200,500]"/>
    <div class="addBtn">
        <el-button :icon="Plus" size="small" round @click="showConfigAddDialog">{{langData.btnAdd}}
        </el-button>
    </div>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle" draggable width="400px">
      <el-form :model="configForm" label-position="right" size="small" :inline="false" ref="configFormRef"
               :rules="configRules"
               label-width="30%">
        <el-form-item :label="langData.configListTableConfigKey" prop="configKey">
          <el-input v-model="configForm.configKey" type="textarea" rows="2"
                    :disabled="dialogTitle == langData.dialogTitleEdit"/>
        </el-form-item>
        <el-form-item :label="langData.configListTableConfigValue" prop="url">
          <el-input v-model="configForm.configValue" type="textarea" rows="5"/>
        </el-form-item>
        <el-form-item :label="langData.configListTableDataType" prop="dataType">
          <el-select v-model="configForm.dataType" size="small" clearable filterable style="width: 100%">
            <el-option :key="item.key" :label="item.value" :value="item.key" v-for="item in dataType"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible = false">{{ langData.btnCancel }}</el-button>
                  <el-button type="primary" @click="updateConfig(configFormRef)">{{ langData.btnSave }}</el-button>
                </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.container {
  flex-grow: 1;
  padding: 20px 2%;
  overflow: auto;
  width: 96%;
}
.addBtn {
  margin-top: 5px;
  text-align: center;
}
</style>
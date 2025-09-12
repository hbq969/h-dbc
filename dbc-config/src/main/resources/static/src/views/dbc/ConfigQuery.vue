<script lang="ts" setup>
import {
  ArrowLeft, EditPen, Delete, Search
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessageBox} from 'element-plus'
import type {FormInstance, FormRules, TableInstance, Action} from 'element-plus'
import router from "@/router";
import {getLangData} from "@/i18n/locale";

const langData = getLangData()

onMounted(() => {
  initial()
});

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const form = reactive({
  configKey: ''
})

const data = reactive({
  configList: []
})

const formRef = ref<FormInstance>();
const rules = reactive<FormRules>({
  configKey: [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}],
})

const queryConfigList = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      queryConfigList2()
    }
  })
}

const queryConfigList2 = () => {
  axios({
    url: '/config/list/services',
    method: 'post',
    data: form,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.configList = res.data.body
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const dataType = ref([])
const initial = () => {
  let fromPage = router.currentRoute.value.query.fromPage
  if (fromPage == 'configList') {
    form.configKey = router.currentRoute.value.query.configKey
    queryConfigList2()
  }
  queryInitial()
}

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

const tableRef = ref<TableInstance>()

const dialogFormVisible = ref(false)
const dialogTitle = ref(langData.dialogTitleEdit)
const configForm = reactive({
  asp: {
    username: '',
    serviceId: '',
    profileName: '',
  },
  config: {
    configKey: '',
    configValue: '',
    dataType: 'java.lang.String'
  }
})
const showConfigEditDialog = (scope) => {
  dialogFormVisible.value = true
  configForm.asp.username = scope.row.username
  configForm.asp.serviceId = scope.row.serviceId
  configForm.asp.profileName = scope.row.profileName
  configForm.config.configKey = scope.row.configKey
  configForm.config.configValue = scope.row.configValue
  configForm.config.dataType = scope.row.dataType
}

const configFormRef = ref<FormInstance>();
const configRules = reactive<FormRules>({
  "config.configKey": [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}],
  "config.configValue": [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}]
})

const updateConfig = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/config',
        method: 'put',
        data: configForm,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, "success")
          dialogFormVisible.value = false
          queryConfigList2()
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
  configForm.asp.username = scope.row.username
  configForm.asp.serviceId = scope.row.serviceId
  configForm.asp.profileName = scope.row.profileName
  configForm.config.configKey = scope.row.configKey
  configForm.config.configValue = scope.row.configValue
  axios({
    url: '/config',
    method: 'delete',
    data: configForm,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryConfigList2()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const dialogFormVisible2 = ref(false)
const configForm2 = reactive({
  asp: {
    username: '',
    serviceId: '',
    profileName: '',
  },
  config: {
    configKey: '',
    configValue: '',
    dataType: 'java.lang.String'
  },
  rows: []
})
const showBatchUpdateConfigDialog = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert(langData.configQueryMsgBoxAlert1, langData.msgBoxTitle, {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  let row = rows[0]
  dialogFormVisible2.value = true
  configForm2.asp.username = row.username
  configForm2.asp.serviceId = row.serviceId
  configForm2.asp.profileName = row.profileName
  configForm2.config.configKey = row.configKey
  configForm2.config.configValue = row.configValue
  configForm2.config.dataType = row.dataType
  configForm2.rows = rows
}

const configFormRef2 = ref<FormInstance>();
const configRules2 = reactive<FormRules>({
  "config.configKey": [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}],
  "config.configValue": [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}]
})

const batchUpdateConfig = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      configForm2.rows.forEach(item => {
        item.configValue = configForm2.config.configValue
        item.dataType = configForm2.config.dataType
      })
      axios({
        url: '/config/batch',
        method: 'put',
        data: configForm2.rows,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, "success")
          dialogFormVisible2.value = false
          queryConfigList2()
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

const batchDeleteConfig = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert(langData.configQueryMsgBoxAlert2, langData.msgBoxTitle, {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  axios({
    url: '/config/batch',
    method: 'delete',
    data: rows,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, "success")
      queryConfigList2()
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
    <el-page-header :icon="ArrowLeft" @back="router.back()">
      <template #content>
        <span class="text-large font-600 mr-3" style="font-size: 15px"> {{ langData.configQueryHeaderTitle }} </span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0"></el-divider>
    <el-form :model="form" size="small" label-position="right" inline-message inline ref="formRef" :rules="rules">
      <el-form-item :label="langData.configListTableConfigKey" prop="configKey">
        <el-input v-model="form.configKey" :placeholder="langData.configQueryInputAccurateMatch" type="text" clearable
                  style="width: 400px"/>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" size="small" @click="queryConfigList(formRef)">
          {{ langData.btnSearch }}
        </el-button>
        <el-button :icon="EditPen" type="warning" size="small" @click="showBatchUpdateConfigDialog">
          {{ langData.configQueryBatchUpdate }}
        </el-button>
        <el-popconfirm :title="langData.configQueryBatchDeleteConfirmTitle" confirm-button-type="danger"
                       @confirm="batchDeleteConfig">
          <template #reference>
            <el-button :icon="Delete" type="danger" size="small">{{ langData.configListTableBatchDelete }}</el-button>
          </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table :data="data.configList" style="width: 100%" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle" ref="tableRef">
      <el-table-column type="selection" header-align="center" align="center"/>
      <el-table-column fixed="left" :label="langData.tableHeaderOp" width="80" header-align="center" align="center">
        <template #default="scope">
          <el-icon @click="showConfigEditDialog(scope)" color="#3F9EFF" style="cursor: pointer; margin-left: 10px" :size="14"><EditPen/></el-icon>
          <el-popconfirm :title="langData.confirmDelete" @confirm="deleteConfig(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-icon color="red" style="cursor: pointer; margin-left: 10px" :size="14"><Delete/></el-icon>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="configKey" :label="langData.configListTableConfigKey" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="300"/>
      <el-table-column prop="configValue" :label="langData.configListTableConfigValue" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center"/>
      <el-table-column prop="serviceName" :label="langData.serviceFormServiceName" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="200"/>
      <el-table-column prop="profileName" :label="langData.configProfileProfileName" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="100"/>
      <el-table-column prop="fmtCreatedAt" :label="langData.tableHeaderCreateTime" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="150"/>
      <el-table-column prop="username" :label="langData.tableHeaderCreator" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="80"/>
      <el-table-column prop="fmtUpdatedAt" :label="langData.tableHeaderUpdateTime" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center" width="150"/>
    </el-table>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle" draggable width="400px">
      <el-form :model="configForm" label-position="right" size="small" :inline="false" ref="configFormRef"
               :rules="configRules"
               label-width="30%">
        <el-form-item :label="langData.configListTableConfigKey" prop="configKey">
          <el-input v-model="configForm.config.configKey" type="textarea" rows="2" disabled/>
        </el-form-item>
        <el-form-item :label="langData.configListTableConfigValue" prop="configValue">
          <el-input v-model="configForm.config.configValue" type="textarea" rows="5"/>
        </el-form-item>
        <el-form-item :label="langData.configListTableDataType" prop="dataType">
          <el-select v-model="configForm.config.dataType" size="small" clearable filterable style="width: 100%">
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

    <el-dialog v-model="dialogFormVisible2" :title="langData.configQueryBatchUpdate" draggable width="400px">
      <el-form :model="configForm2" label-position="right" size="small" :inline="false" ref="configFormRef2"
               :rules="configRules2"
               label-width="30%">
        <el-form-item :label="langData.configListTableConfigKey" prop="configKey">
          <el-input v-model="configForm2.config.configKey" type="textarea" rows="2" disabled/>
        </el-form-item>
        <el-form-item :label="langData.configListTableConfigValue" prop="configValue">
          <el-input v-model="configForm2.config.configValue" type="textarea" rows="5"/>
        </el-form-item>
        <el-form-item :label="langData.configListTableDataType" prop="dataType">
          <el-select v-model="configForm2.config.dataType" size="small" clearable filterable style="width: 100%">
            <el-option :key="item.key" :label="item.value" :value="item.key" v-for="item in dataType"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogFormVisible2 = false">{{ langData.btnCancel }}</el-button>
            <el-popconfirm :title="langData.configQueryBatchModifyConfirmTitle" confirm-button-type="warning"
                           @confirm="batchUpdateConfig(configFormRef2)">
                <template #reference>
                  <el-button type="primary">{{ langData.btnSave }}</el-button>
                </template>
            </el-popconfirm>
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
</style>
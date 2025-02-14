<script lang="ts" setup xmlns="http://www.w3.org/1999/html">
import {
  Edit, ArrowLeft, UploadFilled, Delete
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg, notify} from '@/utils/Utils'
import type {FormInstance, FormRules, UploadInstance, TableInstance} from 'element-plus'
import router from "@/router";

onMounted(() => {
  queryConfigList()
});

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
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref('新增配置')
const configForm = reactive({
  configKey: '',
  configValue: ''
})
const showConfigAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = '新增配置'
  configForm.configKey = ''
  configForm.configValue = ''
}

const showConfigEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = '编辑配置'
  configForm.configKey = scope.row.configKey
  configForm.configValue = scope.row.configValue
}

const configFormRef = ref<FormInstance>();
const configRules = reactive<FormRules>({
  configKey: [{required: true, message: '不能为空', trigger: 'blur'}],
  configValue: [{required: true, message: '不能为空', trigger: 'blur'}]
})

const updateConfig = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/config',
        method: dialogTitle.value == '新增配置' ? 'post' : 'put',
        data: {
          asp: router.currentRoute.value.query,
          config: configForm
        },
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          dialogFormVisible.value = false
          queryConfigList()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
      })
    }
  })
}

const dialogFormVisible2 = ref(false)
const dialogTitle2 = ref('导入配置')
const importForm = reactive({
  file: null,
  cover: 'N'
})
const uploadRef = ref<UploadInstance>()
const fileChange = (uploadFile: UploadFile) => {
  importForm.file = uploadFile.raw
  console.log("选择文件: ", uploadFile.raw)
}
const fileRemove = (uploadFile: UploadFile) => {
  importForm.file = null
  console.log("选择文件: ", uploadFile.raw)
}
const configImport = () => {
  if (importForm.file == null) {
    alert('请先选择导入的文件')
    return
  }
  const formData = new FormData();
  formData.append("file", importForm.file)
  formData.append("cover", importForm.cover)
  let query = router.currentRoute.value.query
  formData.append("username", query.username)
  formData.append("serviceId", query.serviceId)
  formData.append("profileName", query.profileName)
  axios({
    url: '/config/import',
    method: 'post',
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data"
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      dialogFormVisible2.value = false
      uploadRef.value!.clearFiles()
      queryConfigList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}
const showConfigImportDialog = () => {
  dialogFormVisible2.value = true
  importForm.file = null
  importForm.cover = 'N'
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
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const multipleTableRef = ref<TableInstance>()
const selectAll = ref(false)
const deleteMultipleConfig = () => {
  let selectionRows = multipleTableRef.value?.getSelectionRows()
  if (!selectionRows || selectionRows.length == 0) {
    alert('请选择需要删除的配置')
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
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
        <span class="text-large font-600 mr-3"> 配置管理，创建者：{{ router.currentRoute.value.query.username }}，服务: {{
            router.currentRoute.value.query.serviceName
          }}，环境: {{
            router.currentRoute.value.query.profileName
          }}({{ router.currentRoute.value.query.profileDesc }}) </span>
      </template>
    </el-page-header>
    <el-divider content-position="left"></el-divider>

    <el-form :model="form" size="small" label-position="right" inline-message inline>
      <el-form-item label="属性名称" prop="configKey">
        <el-input v-model="form.configKey" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item label="属性值" prop="configValue">
        <el-input v-model="form.configValue" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryConfigList()">查询</el-button>
        <el-button type="success" :icon="Edit" circle @click="showConfigAddDialog()" title="新增角色"/>
        <el-button type="warning" :icon="UploadFilled" circle @click="showConfigImportDialog()" title="导入配置"/>
        <el-popconfirm title="确认是否批量删除这些配置?" confirm-button-type="danger" @confirm="deleteMultipleConfig()">
            <template #reference>
              <el-button type="danger" :icon="Delete" circle title="批量删除配置"/>
            </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table ref="multipleTableRef" :select-on-indeterminate="selectAll" :data="data.configList" style="width: 100%"
              :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column type="selection" header-align="center"
                       align="center"/>
      <el-table-column fixed="left" label="操作" width="100" header-align="center" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="showConfigEditDialog(scope)">编辑
          </el-button>
          <el-popconfirm title="你确定要删除本条记录吗?" @confirm="deleteConfig(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button link type="danger" size="small">删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="configKey" label="属性名称" :show-overflow-tooltip="true" header-align="center"
                       align="left"/>
      <el-table-column prop="configValue" label="属性值" :show-overflow-tooltip="true" header-align="center"
                       align="left"/>
      <el-table-column prop="fmtCreatedAt" label="创建时间" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="180"/>
      <el-table-column prop="fmtUpdatedAt" label="修改时间" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="180"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                   @size-change="queryConfigList()"
                   @current-change="queryConfigList()" @prev-click="queryConfigList()" @next-click="queryConfigList()"
                   :small="true" :background="true"
                   :page-sizes="[5, 10, 20, 50, 100,200,500]"/>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle" draggable>
      <el-form :model="configForm" label-position="right" size="small" :inline="false" ref="configFormRef"
               :rules="configRules"
               label-width="20%">
        <el-form-item label="属性名称：" prop="configKey">
          <el-input v-model="configForm.configKey" type="textarea" rows="3" :disabled="dialogTitle == '编辑配置'"/>
        </el-form-item>
        <el-form-item label="属性值：" prop="url">
          <el-input v-model="configForm.configValue" type="textarea" rows="3"/>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible = false">取消</el-button>
                  <el-button type="primary" @click="updateConfig(configFormRef)">保存</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible2" :title="dialogTitle2" draggable>
      <el-form :model="importForm" label-position="left" size="small" :inline="false" label-width="10%">
        <div>
          <el-upload
              action="#"
              name="file"
              ref="uploadRef"
              class="upload-demo"
              drag
              :multiple="false"
              :auto-upload="false"
              :limit="1"
              @change="fileChange"
              @remove="fileRemove"
              accept=".yml,.yaml,.properties"
          >
            <el-icon class="el-icon--upload">
              <upload-filled/>
            </el-icon>
            <div class="el-upload__text">
              拖动文件到这里 或 <em>点击上传图标</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持文件格式: yml、yaml、properties，文件大小最大1mb
              </div>
            </template>
          </el-upload>
        </div>
        <br/>
        <el-form-item label="是否覆盖" prop="cover">
          <el-switch
              v-model="importForm.cover"
              inline-prompt
              active-text="是"
              inactive-text="否"
              active-value="Y"
              inactive-value="N"
          />
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogFormVisible2 = false">取消</el-button>
            <el-button type="primary" @click="configImport()">保存</el-button>
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
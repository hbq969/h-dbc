<script lang="ts" setup>
import {
  Edit, ArrowLeft, EditPen, Delete, Search
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessage, ElMessageBox} from 'element-plus'
import type {FormInstance, FormRules, TableInstance, Action} from 'element-plus'
import router from "@/router";

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
  configKey: [{required: true, message: '不能为空', trigger: 'blur'}],
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
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const initial=()=>{
  let fromPage = router.currentRoute.value.query.fromPage
  if(fromPage=='configList'){
    form.configKey=router.currentRoute.value.query.configKey
    queryConfigList2()
  }
}

const tableRef = ref<TableInstance>()

const dialogFormVisible = ref(false)
const dialogTitle = ref('编辑配置')
const configForm = reactive({
  asp: {
    username: '',
    serviceId: '',
    profileName: '',
  },
  config: {
    configKey: '',
    configValue: ''
  }
})
const showConfigEditDialog = (scope) => {
  dialogFormVisible.value = true
  configForm.asp.username = scope.row.username
  configForm.asp.serviceId = scope.row.serviceId
  configForm.asp.profileName = scope.row.profileName
  configForm.config.configKey = scope.row.configKey
  configForm.config.configValue = scope.row.configValue
}

const configFormRef = ref<FormInstance>();
const configRules = reactive<FormRules>({
  "config.configKey": [{required: true, message: '不能为空', trigger: 'blur'}],
  "config.configValue": [{required: true, message: '不能为空', trigger: 'blur'}]
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
          dialogFormVisible.value = false
          queryConfigList2()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
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
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
    configValue: ''
  },
  rows: []
})
const showBatchUpdateConfigDialog = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert('请选择选择需要批量修改的配置', '标题', {
      confirmButtonText: 'OK',
      type:'warning',
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
  configForm2.rows = rows
}

const configFormRef2 = ref<FormInstance>();
const configRules2 = reactive<FormRules>({
  "config.configKey": [{required: true, message: '不能为空', trigger: 'blur'}],
  "config.configValue": [{required: true, message: '不能为空', trigger: 'blur'}]
})

const batchUpdateConfig = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      configForm2.rows.forEach(item => item.configValue = configForm2.config.configValue)
      axios({
        url: '/config/batch',
        method: 'put',
        data: configForm2.rows,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          dialogFormVisible2.value = false
          queryConfigList2()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
      })
    }
  })
}

const batchDeleteConfig = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert('请选择选择需要批量删除的配置', '标题', {
      confirmButtonText: 'OK',
      type:'warning',
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
      queryConfigList2()
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
    <el-page-header :icon="ArrowLeft" @back="router.back()">
      <template #content>
        <span class="text-large font-600 mr-3"> 配置批量管理 </span>
      </template>
    </el-page-header>
    <el-divider content-position="left"></el-divider>
    <el-form :model="form" size="small" label-position="right" inline-message inline ref="formRef" :rules="rules">
      <el-form-item label="属性名称" prop="configKey">
        <el-input v-model="form.configKey" placeholder="精确匹配..." type="text" clearable style="width: 400px"/>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" size="small" @click="queryConfigList(formRef)">查询</el-button>
        <el-button :icon="EditPen" type="warning" size="small" @click="showBatchUpdateConfigDialog">批量修改</el-button>
        <el-popconfirm title="请先做好备份，确认是否删除这些配置?" confirm-button-type="danger" @confirm="batchDeleteConfig">
          <template #reference>
            <el-button :icon="Delete" type="danger" size="small">批量删除</el-button>
          </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table :data="data.configList" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle" ref="tableRef">
      <el-table-column type="selection" header-align="center" align="center"/>
      <el-table-column fixed="left" label="操作" width="100" header-align="center" align="center">
        <template #default="scope">
          <el-button circle :icon="EditPen" type="primary" size="small" @click="showConfigEditDialog(scope)"/>
          <el-popconfirm title="请先做好备份，确定要删除本条记录吗?" @confirm="deleteConfig(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button circle :icon="Delete" type="danger" size="small"/>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="configKey" label="属性名" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="300"/>
      <el-table-column prop="configValue" label="属性值" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceName" label="服务名称" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="200"/>
      <el-table-column prop="profileName" label="环境名称" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="100"/>
      <el-table-column prop="fmtCreatedAt" label="创建时间" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="150"/>
      <el-table-column prop="username" label="创建者" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="80"/>
      <el-table-column prop="fmtUpdatedAt" label="修改时间" :show-overflow-tooltip="true" header-align="center"
                       align="center" width="150"/>
    </el-table>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle" draggable width="400px">
      <el-form :model="configForm" label-position="right" size="small" :inline="false" ref="configFormRef"
               :rules="configRules"
               label-width="30%">
        <el-form-item label="属性名称：" prop="configKey">
          <el-input v-model="configForm.config.configKey" type="textarea" rows="2" disabled/>
        </el-form-item>
        <el-form-item label="属性值：" prop="configValue">
          <el-input v-model="configForm.config.configValue" type="textarea" rows="5"/>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible = false">取消</el-button>
                  <el-button type="primary" @click="updateConfig(configFormRef)">保存</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible2" title="批量修改" draggable width="400px">
      <el-form :model="configForm2" label-position="right" size="small" :inline="false" ref="configFormRef2"
               :rules="configRules2"
               label-width="30%">
        <el-form-item label="属性名称：" prop="configKey">
          <el-input v-model="configForm2.config.configKey" type="textarea" rows="2" disabled/>
        </el-form-item>
        <el-form-item label="属性值：" prop="configValue">
          <el-input v-model="configForm2.config.configValue" type="textarea" rows="5"/>
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogFormVisible2 = false">取消</el-button>
            <el-popconfirm title="请先做好备份，确认是否修改?" confirm-button-type="warning" @confirm="batchUpdateConfig(configFormRef2)">
                <template #reference>
                  <el-button type="primary">保存</el-button>
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
<script lang="ts" setup>
import {
  Edit, ArrowLeft, Delete,Grid, ZoomIn, EditPen, Search
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import router from "@/router";
import {getLangData} from "@/i18n/locale";

const langData = getLangData()

const user = reactive({
  userName: '',
  roleName: '',
})

onMounted(() => {
  axios({
    url: '/user',
    method: 'get',
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      user.userName = res.data.body.userName
      user.roleName = res.data.body.roleName
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
  })
  queryServiceList()
});

const form = reactive({
  pageNum: 1,
  pageSize: 10,
  serviceName: '',
  serviceDesc: '',
  username: '',
})
const data = reactive({
  serviceList: [],
  total: 0
})

const queryServiceList = () => {
  axios({
    url: '/service/list',
    method: 'post',
    data: form,
    params: {
      pageNum: form.pageNum,
      pageSize: form.pageSize
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.total = res.data.body.total
      data.serviceList = res.data.body.list
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref(langData.dialogTitleAdd)
const serviceForm = reactive({
  username: '',
  serviceId: '',
  serviceName: '',
  serviceDesc: ''
})
const serviceRef = ref<FormInstance>();
const serviceRules = reactive<FormRules>({
  serviceName: [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}],
})
const updateService = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/service',
        method: dialogTitle.value == langData.dialogTitleAdd ? 'post' : 'put',
        data: serviceForm,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, 'success')
          dialogFormVisible.value = false
          queryServiceList()
        } else {
          let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
          msg(content, "warning")
        }
      }).catch((err: Error) => {
        console.log('',err)
        msg(langData.axiosRequestErr, 'error')
      })
    }
  })
}
const showServiceAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = langData.dialogTitleAdd
  serviceForm.username=''
  serviceForm.serviceId = ''
  serviceForm.serviceName = ''
  serviceForm.serviceDesc = ''
}

const showServiceEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = langData.dialogTitleEdit
  serviceForm.username=scope.row.username
  serviceForm.serviceId = scope.row.serviceId
  serviceForm.serviceName = scope.row.serviceName
  serviceForm.serviceDesc = scope.row.serviceDesc
}

const deleteService = (scope) => {
  axios({
    url: '/service',
    method: 'delete',
    data: {
      username: scope.row.username,
      serviceId: scope.row.serviceId,
      serviceName: scope.row.serviceName
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryServiceList()
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
  })
}

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
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
    <el-form :model="form" size="small" label-position="right" inline-message inline>
      <el-form-item :label="langData.serviceFormUsername" prop="username">
        <el-input v-model="form.username" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item :label="langData.serviceFormServiceName" prop="serviceName">
        <el-input v-model="form.serviceName" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item :label="langData.serviceFormServiceDesc" prop="serviceDesc">
        <el-input v-model="form.serviceDesc" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryServiceList()" :icon="Search">{{langData.btnSearch}}</el-button>
        <el-button type="success" :icon="EditPen" @click="showServiceAddDialog()">{{langData.btnAdd}}</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="data.serviceList" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="180" header-align="center" align="center">
        <template #default="scope">
          <el-button circle :icon="EditPen" :title="langData.btnEdit" type="success" size="small" @click="showServiceEditDialog(scope)" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName"/>
          <el-popconfirm :title="langData.confirmDelete" @confirm="deleteService(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button circle :icon="Delete" :title="langData.btnDelete" type="danger" size="small" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName"/>
            </template>
          </el-popconfirm>
          <el-tooltip :content="langData.serviceTableOpConfigManage" effect="dark" placement="top">
            <el-button circle :icon="Grid" type="success" size="small" @click="router.push({path:'/config/profile',query:scope.row})" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName"/>
          </el-tooltip>
          <el-tooltip :content="langData.serviceTableOpConfigCompare" effect="dark" placement="top">
            <el-button circle :icon="ZoomIn" type="primary" size="small" @click="router.push({path:'/config/compare',query:scope.row})" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName"/>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="username" :label="langData.tableHeaderCreator" :show-overflow-tooltip="true" header-align="center"
                       align="center" v-if="user.roleName=='ADMIN'"/>
      <el-table-column prop="serviceId" label="服务ID" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceName" label="服务名称" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceDesc" label="服务描述" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" :label="langData.tableHeaderCreateTime" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtUpdatedAt" :label="langData.tableHeaderUpdateTime" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                   @size-change="queryServiceList()"
                   @current-change="queryServiceList()" @prev-click="queryServiceList()"
                   @next-click="queryServiceList()"
                   :small="true" :background="true"
                   :page-sizes="[5, 10, 20, 50, 100]"/>
  </div>

  <el-dialog v-model="dialogFormVisible" :title="dialogTitle" draggable width="400px">
    <el-form :model="serviceForm" label-position="right" size="small" :inline="false" ref="serviceRef"
             :rules="serviceRules" label-width="28%">
      <el-form-item :label="langData.serviceTableServiceName" prop="serviceName">
        <el-input v-model="serviceForm.serviceName" type="text" clearable :disabled="dialogTitle==langData.dialogTitleEdit"/>
      </el-form-item>
      <el-form-item :label="langData.serviceTableServiceDesc" prop="serviceDesc">
        <el-input v-model="serviceForm.serviceDesc" type="textarea" rows="2" clearable/>
      </el-form-item>
    </el-form>
    <template #footer>
              <span class="dialog-footer">
                <el-button @click="dialogFormVisible = false">{{langData.btnCancel}}</el-button>
                <el-button type="primary" @click="updateService(serviceRef)">{{langData.btnCancel}}</el-button>
              </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.container {
  flex-grow: 1;
  padding: 20px 2%;
  overflow: auto;
  width: 96%;
}
</style>
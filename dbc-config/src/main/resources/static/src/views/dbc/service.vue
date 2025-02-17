<script lang="ts" setup>
import {
  Edit, ArrowLeft
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import router from "@/router";

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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref('新增服务')
const serviceForm = reactive({
  username: '',
  serviceId: '',
  serviceName: '',
  serviceDesc: ''
})
const serviceRef = ref<FormInstance>();
const serviceRules = reactive<FormRules>({
  serviceName: [{required: true, message: '不能为空', trigger: 'blur'}],
})
const updateService = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/service',
        method: dialogTitle.value == '新增服务' ? 'post' : 'put',
        data: serviceForm,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, 'success')
          dialogFormVisible.value = false
          queryServiceList()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
      })
    }
  })
}
const showServiceAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = '新增服务'
  serviceForm.username=''
  serviceForm.serviceId = ''
  serviceForm.serviceName = ''
  serviceForm.serviceDesc = ''
}

const showServiceEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = '编辑服务'
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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
      <el-form-item label="账号名称" prop="username" v-if="user.roleName=='ADMIN'">
        <el-input v-model="form.username" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item label="服务名称" prop="serviceName">
        <el-input v-model="form.serviceName" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item label="服务描述" prop="serviceDesc">
        <el-input v-model="form.serviceDesc" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryServiceList()">查询</el-button>
        <el-button type="success" :icon="Edit" circle @click="showServiceAddDialog()" title="新增服务"/>
      </el-form-item>
    </el-form>

    <el-table :data="data.serviceList" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="180" header-align="center" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="showServiceEditDialog(scope)" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName">编辑
          </el-button>
          <el-popconfirm title="你确定要删除本条记录吗?" @confirm="deleteService(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button link type="danger" size="small" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName">删除
              </el-button>
            </template>
          </el-popconfirm>
          <el-button link type="success" size="small" @click="router.push({path:'/config/profile',query:scope.row})" :disabled="user.roleName!='ADMIN' && scope.row.username!=user.userName">配置管理
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="创建者" :show-overflow-tooltip="true" header-align="center"
                       align="center" v-if="user.roleName=='ADMIN'"/>
      <el-table-column prop="serviceId" label="服务ID" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceName" label="服务名称" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceDesc" label="服务描述" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="username" label="创建者" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" label="创建时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtUpdatedAt" label="修改时间" :show-overflow-tooltip="true" header-align="center"
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
      <el-form-item label="服务名称：" prop="serviceName">
        <el-input v-model="serviceForm.serviceName" type="text" clearable/>
      </el-form-item>
      <el-form-item label="服务描述：" prop="serviceDesc">
        <el-input v-model="serviceForm.serviceDesc" type="textarea" rows="2" clearable/>
      </el-form-item>
    </el-form>
    <template #footer>
              <span class="dialog-footer">
                <el-button @click="dialogFormVisible = false">取消</el-button>
                <el-button type="primary" @click="updateService(serviceRef)">保存</el-button>
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
<script lang="ts" setup>
import {
  Edit, ArrowLeft
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessage, ElMessageBox} from 'element-plus'
import type {FormInstance, FormRules, TableInstance} from 'element-plus'
import router from "@/router";

const user = reactive({
  userName: '',
  roleName: '',
})

const queryUserInfo = () => {
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
}

onMounted(() => {
  queryProfileList()
  queryBackupList()
  queryUserInfo()
});

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const form = reactive({
  pageNum: 1,
  pageSize: 10,
  serviceName: '',
  profileName: ''
})
const data = reactive({
  profiles: [],
  backups: [],
  total: 0
})
const queryProfileList = () => {
  axios({
    url: '/backup/profile/list',
    method: 'get',
    data: {},
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.profiles = res.data.body
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const tableRef = ref<TableInstance>()

const queryBackupList = () => {
  axios({
    url: '/backup/list',
    method: 'post',
    data: form,
    params: {
      pageNum: form.pageNum,
      pageSize: form.pageSize
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.total = res.data.body.total
      data.backups = res.data.body.list
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const recovery = (scope) => {
  axios({
    url: '/backup/recovery',
    method: 'post',
    data: scope.row,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const deleteBackup = (scope) => {
  axios({
    url: '/backup',
    method: 'delete',
    data: scope.row,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryBackupList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const batchDeleteBackup = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert('请选择需要删除的备份记录', '标题', {
      confirmButtonText: 'OK',
      type: 'warning'
    })
    return
  }
  axios({
    url: '/backup/batch',
    method: 'delete',
    data: {
      username: user.userName,
      backups: rows
    },
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryBackupList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const batchRecovery = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert('请选择需要恢复的备份记录', '标题', {
      confirmButtonText: 'OK',
      type: 'warning'
    })
    return
  }
  axios({
    url: '/backup/recovery/batch',
    method: 'post',
    data: {
      username: user.userName,
      recoveries: rows
    },
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryBackupList()
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
    <el-form :model="form" size="small" label-position="right" inline-message inline>
      <el-form-item label="服务名称" prop="serviceName">
        <el-input v-model="form.serviceName" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item label="环境名称" prop="profileName">
        <el-select v-model="form.profileName" placeholder="请选择" size="small" clearable filterable
                   style="width:120px">
          <el-option :key="item.profileName" :label="item.profileName+'('+item.profileDesc+')'"
                     :value="item.profileName" v-for="item in data.profiles"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryBackupList()">查询</el-button>
        <el-popconfirm title="确认是否删除?" confirm-button-type="danger" @confirm="batchDeleteBackup">
          <template #reference>
            <el-button type="warning" size="small">批量删除</el-button>
          </template>
        </el-popconfirm>
        <el-popconfirm title="确认是否恢复?" confirm-button-type="danger" @confirm="batchRecovery">
          <template #reference>
            <el-button type="warning" size="small">批量恢复</el-button>
          </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="data.backups" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column type="selection" header-align="center" align="center"/>
      <el-table-column fixed="left" label="操作" width="100" header-align="center" align="center">
        <template #default="scope">
          <el-popconfirm title="此操作会覆盖原有配置，请确认?" confirm-button-type="warning" @confirm="recovery(scope)">
            <template #reference>
              <el-button link type="primary" size="small"
                         :disabled="user.roleName!='ADMIN' && user.userName!=scope.row.username">恢复
              </el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="你确定要删除本条记录吗?" @confirm="deleteBackup(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button link type="danger" size="small"
                         :disabled="user.roleName!='ADMIN' && user.userName!=scope.row.username">删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="username" label="配置创建者" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceName" label="服务名称" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="serviceDesc" label="服务描述" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="profileName" label="环境名称" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="profileDesc" label="环境描述" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" label="备份时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                   @size-change="queryBackupList()"
                   @current-change="queryBackupList()" @prev-click="queryBackupList()" @next-click="queryBackupList()"
                   :small="true" :background="true"
                   :page-sizes="[5, 10, 20, 50, 100]"/>
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
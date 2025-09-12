<script lang="ts" setup>
import {
  Edit, ArrowLeft, RefreshRight, Search, Delete, ZoomIn
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessageBox} from 'element-plus'
import type {TableInstance} from 'element-plus'
import {getLangData} from "@/i18n/locale";
import router from "@/router";

const langData = getLangData()

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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

onMounted(() => {
  queryProfileList()
  backfillByQuery()
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const backfillByQuery = () => {
  let info = router.currentRoute.value.query
  if (info) {
    form.serviceName = info.serviceName
    form.profileName = info.profileName
  }
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const batchDeleteBackup = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert(langData.backupMsgBoxAlert1, langData.msgBoxTitle, {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const batchRecovery = () => {
  let rows = tableRef.value?.getSelectionRows()
  if (!rows || rows.length == 0) {
    ElMessageBox.alert(langData.backupMsgBoxAlert2, langData.msgBoxTitle, {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
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
    <el-page-header :icon="ArrowLeft" @back="router.back()" v-if="router.currentRoute.value.query && router.currentRoute.value.query.serviceName">
      <template #content>
        <span class="text-large font-600 mr-3" style="font-size: 15px"> 配置还原 </span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0" v-if="router.currentRoute.value.query && router.currentRoute.value.query.serviceName"></el-divider>
    <el-form :model="form" size="small" label-position="right" inline-message inline>
      <el-form-item :label="langData.configProfileHeaderServiceName" prop="serviceName">
        <el-input v-model="form.serviceName" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item :label="langData.configProfileProfileName" prop="profileName">
        <el-select v-model="form.profileName" :placeholder="langData.formSelectPlaceholder" size="small" clearable
                   filterable
                   style="width:120px">
          <el-option :key="item.profileName" :label="item.profileName+'('+item.profileDesc+')'"
                     :value="item.profileName" v-for="item in data.profiles"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryBackupList()" :icon="Search">{{ langData.btnSearch }}
        </el-button>
        <el-popconfirm :title="langData.backupBatchDeleteConfirmTitle" confirm-button-type="danger" @confirm="batchDeleteBackup">
          <template #reference>
            <el-button type="danger" size="small" :icon="Delete">{{ langData.configListTableBatchDelete }}</el-button>
          </template>
        </el-popconfirm>
        <el-popconfirm :title="langData.backupBatchRecoveryConfirmTitle" confirm-button-type="danger" @confirm="batchRecovery">
          <template #reference>
            <el-button type="warning" size="small" :icon="RefreshRight">{{ langData.backupBatchRecovery }}</el-button>
          </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="data.backups" style="width: 100%" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column type="selection" header-align="center" align="center"/>
      <el-table-column fixed="left" :label="langData.tableHeaderOp" width="120" header-align="center" align="center">
        <template #default="scope">
          <el-tooltip :content="langData.serviceTableOpConfigCompare" effect="dark" placement="top">
<!--            <el-button circle :icon="ZoomIn" type="primary" size="small"-->
<!--                       @click="router.push({path:'/config/compareBackup',query:scope.row})"-->
<!--                       :disabled="user.roleName!='ADMIN' && user.userName!=scope.row.username"/>-->
            <el-icon color="#3F9EFF" style="cursor: pointer; margin-left: 10px" :size="14" @click="router.push({path:'/config/compareBackup',query:scope.row})"
                     :disabled="user.roleName!='ADMIN' && user.userName!=scope.row.username"><ZoomIn/></el-icon>
          </el-tooltip>
          <el-popconfirm :title="langData.backupConfirmTitle" confirm-button-type="warning" @confirm="recovery(scope)">
            <template #reference>
              <el-icon color="#EEBE77" style="cursor: pointer; margin-left: 10px" :size="14" :disabled="user.roleName!='ADMIN' && user.userName!=scope.row.username"
                       :title="langData.backupRecovery"><RefreshRight/></el-icon>
            </template>
          </el-popconfirm>
          <el-popconfirm :title="langData.confirmDelete" @confirm="deleteBackup(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-icon @click="" color="red" style="cursor: pointer; margin-left: 10px" :size="14" :disabled="user.roleName!='ADMIN' && user.userName!=scope.row.username"
                       :title="langData.btnDelete"><Delete/></el-icon>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="username" :label="langData.tableHeaderCreator" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center"/>
      <el-table-column prop="serviceName" :label="langData.configProfileHeaderServiceName" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center"/>
      <el-table-column prop="serviceDesc" :label="langData.serviceFormServiceDesc" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center"/>
      <el-table-column prop="profileName" :label="langData.configProfileProfileName" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center"/>
      <el-table-column prop="profileDesc" :label="langData.configProfileProfileDesc" :show-overflow-tooltip="true"
                       header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" :label="langData.backupTableHeaderBackupTime" :show-overflow-tooltip="true"
                       header-align="center"
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
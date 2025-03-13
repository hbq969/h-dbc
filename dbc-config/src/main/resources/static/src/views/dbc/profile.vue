<script lang="ts" setup>
import {
  Delete,EditPen,Search, CopyDocument
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import {getLangData} from "@/i18n/locale";

const langData = getLangData()

const user = reactive({
  userName: '',
  roleName: ''
})
const getUserInfo = () => {
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
}

onMounted(() => {
  getUserInfo()
  queryProfileList()
});

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const form = reactive({
  pageNum: 1,
  pageSize: 10,
  profileName: '',
  profileDesc: ''
})
const data = reactive({
  total: 0,
  profileList: []
})

const queryProfileList = () => {
  axios({
    url: '/profile/list',
    method: 'post',
    data: form,
    params: {
      pageNum: form.pageNum,
      pageSize: form.pageSize
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.total = res.data.body.total
      data.profileList = res.data.body.list
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
const profileForm = reactive({
  username: '',
  profileName: '',
  profileDesc: ''
})
const profileRef = ref<FormInstance>();
const profileRules = reactive<FormRules>({
  profileName: [{required: true, message: langData.formValidateNotNull, trigger: 'blur'}],
})
const showProfileAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = langData.dialogTitleAdd
  profileForm.username = ''
  profileForm.profileName = ''
  profileForm.profileDesc = ''
}
const showProfileEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = langData.dialogTitleEdit
  profileForm.username = scope.row.username
  profileForm.profileName = scope.row.profileName
  profileForm.profileDesc = scope.row.profileDesc
}
const updateProfile = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/profile',
        method: dialogTitle.value == langData.dialogTitleAdd ? 'post' : 'put',
        data: profileForm,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          dialogFormVisible.value = false
          msg(res.data.body, "success")
          queryProfileList()
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

const deleteProfile = (scope) => {
  axios({
    url: '/profile',
    method: 'delete',
    params: {
      username: scope.row.username,
      profileName: scope.row.profileName
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, "success")
      queryProfileList()
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
  })
}

const backup = (scope) => {
  axios({
    url: '/profile/backup',
    method: 'post',
    data: {
      username: scope.row.username,
      profileName: scope.row.profileName
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
  })
}

const backupAll=()=>{
  axios({
    url: '/profile/backup/all',
    method: 'post'
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
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
      <el-form-item :label="langData.tableHeaderName" prop="profileName">
        <el-input v-model="form.profileName" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item :label="langData.tableHeaderDesc" prop="profileDesc">
        <el-input v-model="form.profileDesc" :placeholder="langData.formInputPlaceholder" type="text" clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryProfileList()" :icon="Search">{{langData.btnSearch}}</el-button>
        <el-popconfirm :title="langData.profileFormBackupAllTips" confirm-button-type="warning" @confirm="backupAll">
          <template #reference>
            <el-button type="success" size="small" v-if="user.roleName=='ADMIN'" :icon="CopyDocument">{{langData.profileFormBackupAll}}</el-button>
          </template>
        </el-popconfirm>
        <el-button type="success" :icon="EditPen" @click="showProfileAddDialog()" v-if="user.roleName=='ADMIN'">{{langData.btnAdd}}</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="data.profileList" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" :label="langData.tableHeaderOp" width="180" header-align="center" align="center"
                       v-if="user.roleName=='ADMIN'">
        <template #default="scope">
          <el-button circle :icon="EditPen" type="success" size="small" @click="showProfileEditDialog(scope)" :title="langData.btnEdit"/>
          <el-popconfirm :title="langData.profileDeleteConfirmTitle" @confirm="deleteProfile(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button circle :icon="Delete" type="danger" size="small" :title="langData.btnDelete"/>
            </template>
          </el-popconfirm>
          <el-popconfirm :title="langData.profileRecoveryConfirmTitle" @confirm="backup(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button circle :icon="CopyDocument" type="success" size="small" :title="langData.profileTableOpBackup"/>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="profileName" :label="langData.tableHeaderName" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="profileDesc" :label="langData.tableHeaderDesc" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="username" :label="langData.profileTableHeaderCreator" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" :label="langData.tableHeaderCreateTime" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtUpdatedAt" :label="langData.tableHeaderUpdateTime" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                   @size-change="queryProfileList()"
                   @current-change="queryProfileList()" @prev-click="queryProfileList()"
                   @next-click="queryProfileList()"
                   :small="true" :background="true"
                   :page-sizes="[5, 10, 20, 50, 100]"/>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle" draggable>
      <el-form :model="profileForm" label-position="right" size="small" :inline="false" ref="profileRef"
               :rules="profileRules"
               label-width="20%">
        <el-form-item :label="langData.tableHeaderName" prop="profileName">
          <el-input v-model="profileForm.profileName" type="text" clearable :disabled="dialogTitle==langData.dialogTitleEdit"/>
        </el-form-item>
        <el-form-item :label="langData.tableHeaderDesc" prop="profileDesc">
          <el-input v-model="profileForm.profileDesc" type="textarea" rows="5" clearable/>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible = false">{{langData.btnCancel}}</el-button>
                  <el-button type="primary" @click="updateProfile(profileRef)">{{langData.btnSave}}</el-button>
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
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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref('新增环境')
const profileForm = reactive({
  username: '',
  profileName: '',
  profileDesc: ''
})
const profileRef = ref<FormInstance>();
const profileRules = reactive<FormRules>({
  profileName: [{required: true, message: '不能为空', trigger: 'blur'}],
})
const showProfileAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = '新增环境'
  profileForm.username = ''
  profileForm.profileName = ''
  profileForm.profileDesc = ''
}
const showProfileEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = '编辑环境'
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
        method: dialogTitle.value == '新增环境' ? 'post' : 'put',
        data: profileForm,
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          dialogFormVisible.value = false
          queryProfileList()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
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
      queryProfileList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
      <el-form-item label="环境名称" prop="profileName">
        <el-input v-model="form.profileName" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item label="环境描述" prop="profileDesc">
        <el-input v-model="form.profileDesc" placeholder="请输入..." type="text" clearable/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryProfileList()">查询</el-button>
        <el-popconfirm title="此操作会备份所有配置，是否确认此操作?" confirm-button-type="warning" @confirm="backupAll">
          <template #reference>
            <el-button type="success" size="small" v-if="user.roleName=='ADMIN'">全量备份</el-button>
          </template>
        </el-popconfirm>
        <el-button type="success" :icon="Edit" circle @click="showProfileAddDialog()" title="新增环境" v-if="user.roleName=='ADMIN'"/>
      </el-form-item>
    </el-form>

    <el-table :data="data.profileList" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="180" header-align="center" align="center"
                       v-if="user.roleName=='ADMIN'">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="showProfileEditDialog(scope)">编辑
          </el-button>
          <el-popconfirm title="此操作会删除本环境关联的所有服务配置?" @confirm="deleteProfile(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button link type="danger" size="small">删除
              </el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="此操作会备份环境下所有服务的配置，是否确认备份?" @confirm="backup(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button link type="success" size="small">备份
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="profileName" label="环境名称" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="profileDesc" label="环境描述" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="username" label="创建账号" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" label="创建时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtUpdatedAt" label="更新时间" :show-overflow-tooltip="true" header-align="center"
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
        <el-form-item label="环境名称：" prop="profileName">
          <el-input v-model="profileForm.profileName" type="text" clearable/>
        </el-form-item>
        <el-form-item label="环境描述：" prop="profileDesc">
          <el-input v-model="profileForm.profileDesc" type="textarea" rows="5" clearable/>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible = false">取消</el-button>
                  <el-button type="primary" @click="updateProfile(profileRef)">保存</el-button>
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
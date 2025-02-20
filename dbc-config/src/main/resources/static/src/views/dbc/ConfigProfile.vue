<script lang="ts" setup>
import {
  Edit, ArrowLeft, Delete, Grid, DocumentCopy, UploadFilled, Download
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessage, ElMessageBox} from 'element-plus'
import type {FormInstance, FormRules, UploadInstance} from 'element-plus'
import router from "@/router";

onMounted(() => {
  queryAllProfileList()
});

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const form = reactive({
  pageNum: 1,
  pageSize: 10,
  serviceId: router.currentRoute.value.query.serviceId,
  username: router.currentRoute.value.query.username,
  profileName: '',
  profileDesc: ''
})
const data = reactive({
  total: 0,
  profileConfigList: [],
  profileList: []
})

const queryAllProfileList = () => {
  axios({
    url: '/profile/all',
    method: 'post',
    data: {
      username: router.currentRoute.value.query.username,
      serviceId: router.currentRoute.value.query.serviceId
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.profileList = res.data.body
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const deleteConfig = (source) => {
  axios({
    url: '/profile/config',
    method: 'delete',
    data: {
      serviceId: router.currentRoute.value.query.serviceId,
      username: router.currentRoute.value.query.username,
      profileName: source.profileName
    },
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryAllProfileList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const goConfigList = (source) => {
  source.serviceId = router.currentRoute.value.query.serviceId
  source.serviceName = router.currentRoute.value.query.serviceName
  source.username = router.currentRoute.value.query.username
  router.push({
    path: '/config/list',
    query: source
  })
}

const goConfigFile = (source) => {
  source.serviceId = router.currentRoute.value.query.serviceId
  source.serviceName = router.currentRoute.value.query.serviceName
  source.username = router.currentRoute.value.query.username
  router.push({
    path: '/config/file',
    query: source
  })
}

const dialogFormVisible2 = ref(false)
const dialogTitle2 = ref('导入配置')
const importForm = reactive({
  file: null,
  cover: 'N',
  backup: 'N',
  serviceId: '',
  username: '',
  profileName: ''
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
    ElMessageBox.alert('请先选择导入的文件', '标题', {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  const formData = new FormData();
  formData.append("file", importForm.file)
  formData.append("cover", importForm.cover)
  formData.append("backup", importForm.backup)
  formData.append("username", importForm.username)
  formData.append("serviceId", importForm.serviceId)
  formData.append("profileName", importForm.profileName)
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
      queryAllProfileList()
    } else {
      msg(res.data.errorMessage, 'warning')
      uploadRef.value!.clearFiles()
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
    uploadRef.value!.clearFiles()
  })
}
const showConfigImportDialog = (source) => {
  dialogFormVisible2.value = true
  importForm.file = null
  importForm.cover = 'N'
  importForm.serviceId = router.currentRoute.value.query.serviceId
  importForm.username = router.currentRoute.value.query.username
  importForm.profileName = source.profileName
}

const dialogFormVisible3 = ref(false)
const dialogTitle3 = ref('下载配置')
const downForm = reactive({
  serviceId: '',
  username: '',
  profileName: '',
  fileSuffix: 'yml',
  getFilename: function () {
    if (!this.profileName || this.profileName == '' || this.profileName == 'default') {
      return "application." + this.fileSuffix;
    } else {
      return "application-" + this.profileName + "." + this.fileSuffix;
    }
  }
})

const showConfigDownloadDialog = (source) => {
  dialogFormVisible3.value = true
  downForm.serviceId = router.currentRoute.value.query.serviceId
  downForm.username = router.currentRoute.value.query.username
  downForm.profileName = source.profileName
}

const downFile = (fileSuffix) => {
  downForm.fileSuffix = fileSuffix
  axios({
    url: '/config/download',
    method: 'post',
    data: downForm,
  }).then((res: any) => {
    const blob = new Blob([res.data]);
    const fileName = downForm.getFilename();
    if ('download' in document.createElement('a')) { // 非IE下载
      const elink = document.createElement('a');
      elink.download = fileName
      elink.style.display = 'none';
      elink.href = URL.createObjectURL(blob);
      document.body.appendChild(elink);
      elink.click();
      URL.revokeObjectURL(elink.href);// 释放URL 对象
      document.body.removeChild(elink);
    } else {
      // IE10+下载
      // if (navigator) {
      //   navigator.msSaveBlob(blob, fileName);
      // }
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
    <el-page-header :icon="ArrowLeft" @back="router.push({path:'/service'})">
      <template #content>
        <span class="text-large font-600 mr-3"> 配置管理，创建者：{{ router.currentRoute.value.query.username }}，服务名：{{
            router.currentRoute.value.query.serviceName
          }}</span>
      </template>
    </el-page-header>
    <el-divider content-position="left"></el-divider>

    <el-space wrap>
      <el-card style="max-width: 280px;height: 220px;" v-for="(source,index) in data.profileList">
        <template #header="scope">
          <div class="card-header" style="display: flex; align-items: center; justify-content: space-between">
            <div style="margin-top: 5px;text-align: center;flex: 1;display: flex; justify-content: left">
              <component is="MenuIcon"/>
            </div>
            <div style="flex: 1; display: flex; justify-content: flex-end">
              <el-popconfirm title="确认清空此环境配置?" confirm-button-type="danger" @confirm="deleteConfig(source)">
                <template #reference>
                  <el-button type="info" size="small" circle :icon="Delete" title="清空配置"/>
                </template>
              </el-popconfirm>
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-tooltip content="导入配置" effect="dark" placement="top">
                  <el-button type="warning" size="small" :icon="UploadFilled" circle
                             @click="showConfigImportDialog(source)"/>
                </el-tooltip>
              </el-badge>
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-tooltip content="下载配置" effect="dark" placement="top">
                  <el-button type="success" size="small" :icon="Download" circle
                             @click="showConfigDownloadDialog(source)"/>
                </el-tooltip>
              </el-badge>
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-tooltip content="查看YAML" effect="dark" placement="top">
                  <el-button type="primary" size="small" circle :icon="DocumentCopy" @click="goConfigFile(source)"/>
                </el-tooltip>
              </el-badge>
              <el-badge :value="source.configNum" style="margin-left: 12px" type="danger" :max="500"
                        :hidden="source.configNum==0">
                <el-tooltip content="查看Properties" effect="dark" placement="top">
                  <el-button type="success" size="small" circle :icon="Grid" @click="goConfigList(source)"/>
                </el-tooltip>
              </el-badge>
            </div>
          </div>
        </template>
        <template #default="scope">
          <el-form size="small" label-position="right" inline-message :inline="false" label-width="100px"
                   style="width:240px">
            <el-form-item label="环境名称：">
              {{ source.profileName }}
            </el-form-item>
            <el-form-item label="环境描述：">
              {{ source.profileDesc }}
            </el-form-item>
          </el-form>
        </template>
      </el-card>
    </el-space>

    <el-dialog v-model="dialogFormVisible2" :title="dialogTitle2" draggable style="height: 460px;width:450px">
      <el-form :model="importForm" label-position="left" size="small" :inline="false" label-width="15%">
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
              style="height:230px"
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
        <el-form-item label="是否覆盖" prop="cover" label-width="80">
          <el-switch
              v-model="importForm.cover"
              inline-prompt
              active-text="是"
              inactive-text="否"
              active-value="Y"
              inactive-value="N"
              @change="importForm.cover=='Y'?importForm.backup='Y':importForm.backup='N'"
          />
        </el-form-item>
        <el-form-item label="是否备份" prop="cover" label-width="80">
          <el-switch
              v-model="importForm.backup"
              inline-prompt
              active-text="是"
              inactive-text="否"
              active-value="Y"
              inactive-value="N"
          />
        </el-form-item>
        <el-form-item label-width="73%">
          <el-button @click="dialogFormVisible2 = false">取消</el-button>
          <el-button type="primary" @click="configImport()">保存</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible3" :title="dialogTitle3" draggable width="300px">
      <div>
        <el-button type="primary" @click="downFile('yml')">YAML</el-button>
        <el-button type="success" @click="downFile('properties')">Properties</el-button>
      </div>
      <template #footer>
            <span class="dialog-footer">
              <el-button @click="dialogFormVisible3 = false">取消</el-button>
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

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 400px;
  height: 366px;
  text-align: center;
}
</style>
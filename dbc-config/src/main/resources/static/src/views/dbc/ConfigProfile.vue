<script lang="ts" setup>
import {
  ArrowLeft, Delete, Grid, DocumentCopy, UploadFilled, Download, CopyDocument, RefreshRight
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessageBox} from 'element-plus'
import type {UploadInstance} from 'element-plus'
import router from "@/router";
import {getLangData} from "@/i18n/locale";

const langData = getLangData()

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
      let content = res.config.baseURL + res.config.url + ': ' + res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('', err)
    msg(langData.axiosRequestErr, 'error')
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
      let content = res.config.baseURL + res.config.url + ': ' + res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('', err)
    msg(langData.axiosRequestErr, 'error')
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
const dialogTitle2 = ref(langData.configProfileImport)
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
    ElMessageBox.alert(langData.configProfileImportAlert, langData.msgBoxTitle, {
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
      let content = res.config.baseURL + res.config.url + ': ' + res.data.errorMessage;
      msg(content, "warning")
      uploadRef.value!.clearFiles()
    }
  }).catch((err: Error) => {
    console.log('', err)
    msg(langData.axiosRequestErr, 'error')
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
const dialogTitle3 = ref(langData.configProfileDialog3Title)
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
    msg(langData.axiosRequestErr, 'error')
  })
}

const downIntegrated = (source, filename) => {
  axios({
    url: '/config/bootstrap/download',
    method: 'post',
    data: {
      filename: filename,
      serviceName: router.currentRoute.value.query.serviceName,
      profileName: source.profileName
    },
  }).then((res: any) => {
    const blob = new Blob([res.data]);
    const fileName = filename;
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
    console.log('', err)
    msg('请求异常', 'error')
  })
}

const dialogFormVisible4 = ref(false)
const dialogTitle4 = ref(langData.configProfileDialogTitle4)
const httpIntegrated=reactive({
  serviceName: '',
  profileName:''
})
const showHttpIntegrated = (source) => {
  dialogFormVisible4.value = true
  httpIntegrated.serviceName=router.currentRoute.value.query.serviceName
  httpIntegrated.profileName=source.profileName
}

const backupConfig = (source) => {
  source.username = router.currentRoute.value.query.username
  source.serviceId = router.currentRoute.value.query.serviceId
  axios({
    url: '/config/backup',
    method: 'post',
    data: source
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
    } else {
      let content = res.config.baseURL + res.config.url + ': ' + res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('', err)
    msg(langData.axiosRequestErr, 'error')
  })
}

const goRecoveryPage = (source) => {
  source.serviceName = router.currentRoute.value.query.serviceName
  router.push({path: '/back/data', query: source})
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
        <span class="text-large font-600 mr-3" style="font-size: 15px"> {{ langData.configProfileHeaderCreator }}：{{
            router.currentRoute.value.query.username
          }}，{{ langData.configProfileHeaderServiceName }}：{{
            router.currentRoute.value.query.serviceName
          }}</span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0"></el-divider>

    <el-space wrap>
      <el-card style="max-width: 300px;height: 220px;" v-for="(source,index) in data.profileList">
        <template #header="scope">
          <div class="card-header" style="display: flex; align-items: center; justify-content: space-between">
            <div style="margin-top: 5px;text-align: center;flex: 1;display: flex; justify-content: left">
              <component is="MenuIcon"/>
            </div>
            <div style="flex: 1; display: flex; justify-content: flex-end">
              <el-popconfirm :title="langData.configProfileDeleteConfirmTitle" confirm-button-type="danger"
                             @confirm="deleteConfig(source)">
                <template #reference>
                  <el-button type="info" size="small" circle :icon="Delete" :title="langData.configProfileClearConfig"/>
                </template>
              </el-popconfirm>
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-tooltip :content="langData.configProfileImport" effect="dark" placement="top">
                  <el-button type="warning" size="small" :icon="UploadFilled" circle
                             @click="showConfigImportDialog(source)"/>
                </el-tooltip>
              </el-badge>
              <!--              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">-->
              <!--                <el-tooltip :content="langData.configProfileDialog3Title" effect="dark" placement="top">-->
              <!--                  <el-button type="success" size="small" :icon="Download" circle-->
              <!--                             @click="showConfigDownloadDialog(source)"/>-->
              <!--                </el-tooltip>-->
              <!--              </el-badge>-->
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-tooltip :content="langData.configProfileRecoveryTitle" effect="dark" placement="top">
                  <el-button circle :icon="RefreshRight" type="warning" size="small" @click="goRecoveryPage(source)"/>
                </el-tooltip>
              </el-badge>
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-tooltip :content="langData.configProfileDetailYaml" effect="dark" placement="top">
                  <el-button type="primary" size="small" circle :icon="DocumentCopy" @click="goConfigFile(source)"/>
                </el-tooltip>
              </el-badge>
              <el-badge :value="0" style="margin-left: 12px" type="danger" :max="500" :hidden="true">
                <el-popconfirm :title="langData.configProfileBackupConfirmTitle" confirm-button-type="warning"
                               @confirm="backupConfig(source)">
                  <template #reference>
                    <el-button type="warning" size="small" circle :icon="CopyDocument"
                               :title="langData.configProfileBackupConfig"/>
                  </template>
                </el-popconfirm>
              </el-badge>
              <el-badge :value="source.configNum" style="margin-left: 12px" type="danger" :max="500"
                        :hidden="source.configNum==0">
                <el-tooltip :content="langData.configProfileDetailProperties" effect="dark" placement="top">
                  <el-button type="success" size="small" circle :icon="Grid" @click="goConfigList(source)"/>
                </el-tooltip>
              </el-badge>
            </div>
          </div>
        </template>
        <template #default="scope">
          <el-form size="small" label-position="right" inline-message :inline="false" label-width="150px"
                   style="width:260px">
            <el-form-item :label="langData.configProfileProfileName">
              {{ source.profileName }}
            </el-form-item>
            <el-form-item :label="langData.configProfileProfileDesc">
              {{ source.profileDesc }}
            </el-form-item>
            <el-form-item :label="langData.configProfileServiceIntegrated">
              <el-tooltip :content="langData.configProfileJavaIntegrated" effect="dark" placement="top">
                <el-link type="success" style="font-size: 1em; margin-right: 5px"
                         @click="downIntegrated(source,'bootstrap'+(source.profileName == 'default' ? '' : '-' + source.profileName)+'.yml')">
                  java
                </el-link>
              </el-tooltip>
              |
              <el-tooltip :content="langData.configProfileHttpIntegrated" effect="dark" placement="top">
                <el-link type="success" style="font-size: 1em; margin-left: 5px" @click="showHttpIntegrated(source)">
                  http
                </el-link>
              </el-tooltip>
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
              {{ langData.configProfileUploadTips1Prefix }} <em> {{ langData.configProfileUploadTips1Suffix }} </em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                {{ langData.configProfileUploadTips2 }}
              </div>
            </template>
          </el-upload>
        </div>
        <br/>
        <el-form-item :label="langData.configProfileIfCover" prop="cover" label-width="80">
          <el-switch
              v-model="importForm.cover"
              inline-prompt
              :active-text="langData.switchYes"
              :inactive-text="langData.switchNo"
              active-value="Y"
              inactive-value="N"
              @change="importForm.cover=='Y'?importForm.backup='Y':importForm.backup='N'"
          />
        </el-form-item>
        <el-form-item :label="langData.configProfileIfBackup" prop="backup" label-width="80">
          <el-switch
              v-model="importForm.backup"
              inline-prompt
              :active-text="langData.switchYes"
              :inactive-text="langData.switchNo"
              active-value="Y"
              inactive-value="N"
          />
        </el-form-item>
        <el-form-item label-width="60%">
          <el-button @click="dialogFormVisible2 = false">{{ langData.btnCancel }}</el-button>
          <el-button type="primary" @click="configImport()">{{ langData.btnSave }}</el-button>
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
              <el-button @click="dialogFormVisible3 = false">{{ langData.btnCancel }}</el-button>
            </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible4" :title="dialogTitle4" draggable width="50%" style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step1" placement="top">
          <el-card>
            <h4>{{langData.configProfileDialog4Step1H4}}</h4>
            <p>
              <pre style="font-size: 0.8em">{"serviceName":"{{httpIntegrated.serviceName}}", "profileName": "{{httpIntegrated.profileName}}", "type": "YAML"}</pre>
            </p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step2" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step2H4 }}</h4>
            <img :src="require('@/assets/img/request_encrypt.png')" style="width: 80%;height: 80%">
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step3" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step3H4 }}</h4>
            <p>Basic认证参数</p>
            <img :src="require('@/assets/img/request_send1.png')" style="width: 80%;height: 80%">
            <p>请求响应</p>
            <img :src="require('@/assets/img/request_send2.png')" style="width: 80%;height: 80%">
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step4" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step4H4 }}</h4>
            <img :src="require('@/assets/img/request_decrypt.png')" style="width: 80%;height: 80%">
            <h4>{{ langData.configProfileDialog4Step4JavascriptExample }}</h4>
            <p><pre style="font-size: 0.8em">import CryptoJS from "crypto-js";

function encrypt(word: any, key: any, iv: any): string {
    let srcs = CryptoJS.enc.Utf8.parse(word);
    let encrypted = CryptoJS.AES.encrypt(srcs, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    return CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
}
let body = {"serviceName":"{{httpIntegrated.serviceName}}", "profileName": "{{httpIntegrated.profileName}}", "type": "YAML"}
let body = encrypt(JSON.stringify(body),key,iv)</pre></p>



            <p><pre style="font-size: 0.8em;word-wrap: break-word; white-space: pre-wrap;">curl --location 'http://账号:密码@localhost:30170/h-dbc/api/config/list' \
--header 'Accept: */*' \
--header 'Content-Type: application/json' \
--data 'hBT29zeISxtLm+......1wxypWIXBzL+fACvVd34Q=='
            </pre></p>
            <h4>{{langData.configProfileDialog4Step3H4Response}}</h4>
            <p><pre style="font-size: 0.8em;word-wrap: break-word; white-space: pre-wrap;">"X6stS92xkYcnKNYYXDaCGvVV/2KQ8W5hQYR7HM6wIWmWZnb8p1UsrOVaZL.....r2If3yYKNbwjUYg17zS96NLZvl8QA30lcNPfFvk20ilvhw=="</pre></p>


            <p><pre style="font-size: 0.8em;word-wrap: break-word; white-space: pre-wrap;">import CryptoJS from "crypto-js";

function decrypt(word: any, key: any, iv: any): string {
    let base64 = CryptoJS.enc.Base64.parse(word);
    let src = CryptoJS.enc.Base64.stringify(base64);
    let decrypt = CryptoJS.AES.decrypt(src, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
    return decryptedStr.toString();
}
let response = "X6stS92xkYcnKNYYXDaCGvVV/2KQ8W5hQYR7HM6wIWmWZnb8p1UsrOVaZL.....r2If3yYKNbwjUYg17zS96NLZvl8QA30lcNPfFvk20ilvhw=="
let config = decrypt(response,key,iv)</pre></p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
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
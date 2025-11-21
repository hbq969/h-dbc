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
import * as monaco from 'monaco-editor';

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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
  backup: 'Y',
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
      msg(res.data.errorMessage, 'warning')
      uploadRef.value!.clearFiles()
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
  }).catch((err: any) => {
    msg(err?.response.data.errorMessage, 'error')
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
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const dialogFormVisible9 = ref(false)
const dialogTitle9 = ref(langData.configProfileDialogTitle9)
const dialogFormVisible4 = ref(false)
const dialogTitle4 = ref(langData.configProfileDialogTitle4)
const dialogFormVisible5 = ref(false)
const dialogTitle5 = ref(langData.configProfileDialogTitle5)
const dialogFormVisible6 = ref(false)
const dialogTitle6 = ref(langData.configProfileDialogTitle6)
const dialogFormVisible7 = ref(false)
const dialogTitle7 = ref(langData.configProfileDialogTitle7)
const dialogFormVisible8 = ref(false)
const dialogTitle8 = ref(langData.configProfileDialogTitle8)

const httpIntegrated = reactive({
  serviceName: '',
  profileName: ''
})
const showHttpIntegrated = (source, lang) => {
  httpIntegrated.serviceName = router.currentRoute.value.query.serviceName
  httpIntegrated.profileName = source.profileName
  if(lang=='java'){
    dialogFormVisible9.value = true
  } else if (lang == 'http') {
    dialogFormVisible4.value = true
  } else if (lang == 'python') {
    dialogFormVisible5.value = true
  } else if (lang == 'js') {
    dialogFormVisible6.value = true
  } else if(lang == 'go'){
    dialogFormVisible7.value = true
  } else if (lang == 'C++') {
    dialogFormVisible8.value = true
  }
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
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
          <el-form size="small" label-position="right" inline-message :inline="false" label-width="80px"
                   style="width:260px">
            <el-form-item :label="langData.configProfileProfileName">
              {{ source.profileName }}
            </el-form-item>
            <el-form-item :label="langData.configProfileProfileDesc">
              {{ source.profileDesc }}
            </el-form-item>
            <el-form-item :label="langData.configProfileServiceIntegrated">
              <el-link type="success" style="font-size: 1em; margin-right: 2px"
                       @click="showHttpIntegrated(source,'java')">
                java
              </el-link>
              |
              <el-link type="success" style="font-size: 1em; margin-left: 2px;margin-right: 2px"
                       @click="showHttpIntegrated(source,'http')">http
              </el-link>
              |
              <el-link type="success" style="font-size: 1em; margin-left: 2px;margin-right: 2px"
                       @click="showHttpIntegrated(source,'python')">python
              </el-link>
              |
              <el-link type="success" style="font-size: 1em; margin-left: 2px;margin-right: 2px"
                       @click="showHttpIntegrated(source,'js')">js
              </el-link>
              |
              <el-link type="success" style="font-size: 1em; margin-left: 2px;margin-right: 2px"
                       @click="showHttpIntegrated(source,'go')">go
              </el-link>
              |
              <el-link type="success" style="font-size: 1em; margin-left: 2px"
                       @click="showHttpIntegrated(source,'C++')">C++
              </el-link>
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
<!--        <el-form-item :label="langData.configProfileIfCover" prop="cover" label-width="80">-->
<!--          <el-switch-->
<!--              v-model="importForm.cover"-->
<!--              inline-prompt-->
<!--              :active-text="langData.switchYes"-->
<!--              :inactive-text="langData.switchNo"-->
<!--              active-value="Y"-->
<!--              inactive-value="N"-->
<!--              @change="importForm.cover=='Y'?importForm.backup='Y':importForm.backup='N'"-->
<!--          />-->
<!--        </el-form-item>-->
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

    <el-dialog v-model="dialogFormVisible3" :title="dialogTitle3" draggable width="70%">
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

    <el-dialog v-model="dialogFormVisible4" :title="dialogTitle4" draggable width="70%"
               style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step1" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step1H4 }}</h4>
            <p>
              <pre style="font-size: 0.8em">curl -XGET 'http://{Username}:{Password}@ip:port/h-dbc/api/publicKey'</pre>
            </p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step2" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step2H4 }}</h4>
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step3" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step3H4 }}</h4>
            <h5>{{ langData.configProfileDialog4Step3TypeDesc }}</h5>
            <pre style="font-size: 0.8em">{
  "key": rsa('{{ langData.configProfileDialog4Step3Key }}', '{{ langData.configProfileDialog4Step3RSAPublicKey }}'),
  "iv": rsa('{{ langData.configProfileDialog4Step3Iv }}', '{{ langData.configProfileDialog4Step3RSAPublicKey }}'),
  "body": aes('{"serviceName":"{{httpIntegrated.serviceName}}","profileName":"{{httpIntegrated.profileName}}", "type":"YAML"}','{{ langData.configProfileDialog4Step3Key }}','{{ langData.configProfileDialog4Step3Iv }}'
}</pre>
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step4" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step4H4 }}</h4>
            <p><pre style="font-size: 0.8em">curl -XPOST 'http://{Username}:{Password}@ip:port/h-dbc/api/config/list' \
-d ‘{
  "key": rsa('{{ langData.configProfileDialog4Step3Key }}', '{{ langData.configProfileDialog4Step3RSAPublicKey }}'),
  "iv": rsa('{{ langData.configProfileDialog4Step3Iv }}', '{{ langData.configProfileDialog4Step3RSAPublicKey }}'),
  "body": aes('{"serviceName":"h-example","profileName":"dev", "type":"YAML"}',
              '{{ langData.configProfileDialog4Step3Key }}',
              '{{ langData.configProfileDialog4Step3Iv }}')
}’ \
-H 'Content-Type:application/json'</pre>
            </p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item :timestamp="langData.configProfileDialog4Step5" placement="top">
          <el-card>
            <h4>{{ langData.configProfileDialog4Step5H4 }}</h4>
            <p><pre style="font-size: 0.8em">aes('{{ langData.configProfileDialog4Step3Key }}',
              '{{ langData.configProfileDialog4Step3Iv }}',
              '{{ langData.configProfileDialog4Step3RSAPublicKey }}')</pre>
            </p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible5" :title="dialogTitle5" draggable width="70%"
               style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item timestamp="config_reader.py" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
import requests
import base64
import json
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import secrets
import string
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_v1_5

class ConfigReader:
    def __init__(self):
        self.username = "{Username}"
        self.password = "{Password}"
        self.service_name = "{{httpIntegrated.serviceName}}"
        self.profile_name = "{{httpIntegrated.profileName}}"
        self.public_key_url = "http://localhost:30170/h-dbc/api/publicKey"
        self.config_url = "http://localhost:30170/h-dbc/api/config/list"
        self.config_format = "PROP"
        self.client = requests.Session()
        self.client.auth = (self.username, self.password)

    def get_public_key(self):
        response = self.client.get(self.public_key_url)
        response.raise_for_status()
        public_key = response.json()['body']
        public_key = f"-----BEGIN PUBLIC KEY-----\n{public_key}\n-----END PUBLIC KEY-----"
        return public_key

    def random_key_with_aes(self, length=16):
        characters = string.ascii_letters + string.digits
        random_string = ''.join(secrets.choice(characters) for _ in range(length))
        return random_string

    def encrypt(self, data, key, iv, encoding='utf-8'):
        cipher = AES.new(key, AES.MODE_CBC, iv)
        return cipher.encrypt(pad(data.encode(encoding), AES.block_size))

    def decrypt(self, data, key, iv, encoding='utf-8'):
        cipher = AES.new(key, AES.MODE_CBC, iv)
        return unpad(cipher.decrypt(data), AES.block_size).decode(encoding)

    def rsa_encrypt(self, data, public_key):
        key = RSA.import_key(public_key)
        cipher = PKCS1_v1_5.new(key)
        encrypted_data = cipher.encrypt(data)
        return base64.b64encode(encrypted_data).decode('utf-8')

    def encode(self, data):
        return base64.b64encode(data).decode('utf-8')

    def decode(self, data):
        return base64.b64decode(data)

    def fetch_config(self):
        public_key = self.get_public_key()
        print('\nGet the RSA public key from the server: \n', public_key)
        aes_key = self.random_key_with_aes(16)
        aes_iv = self.random_key_with_aes(16)
        print('\nAES encryption key: %s \nSymmetric encryption iv: %s' % (aes_key, aes_iv))

        rsa_key_encrypt = self.rsa_encrypt(aes_key.encode('utf-8'), public_key)
        rsa_iv_encrypt = self.rsa_encrypt(aes_iv.encode('utf-8'), public_key)

        app = {
            "serviceName": self.service_name,
            "profileName": self.profile_name,
            "type": self.config_format
        }

        json_app = json.dumps(app, ensure_ascii=False, indent=None)

        body = {
            "key": rsa_key_encrypt,
            "iv": rsa_iv_encrypt,
            "body": self.encode(self.encrypt(json_app, aes_key.encode('utf-8'), aes_iv.encode('utf-8')))
        }

        json_body = json.dumps(body, ensure_ascii=False, indent=None)
        print('\nPull configuration interface request body: \n', json_body)

        headers = {
            'Content-Type': 'application/json'
        }

        response = self.client.post(self.config_url, data=json_body, headers=headers)

        response.raise_for_status()
        response_content = response.content
        print('\nPull configuration request response before decryption: \n', response_content)

        encrypted_bytes = base64.b64decode(response_content)

        decrypted_content = self.decrypt(encrypted_bytes, aes_key.encode('utf-8'), aes_iv.encode('utf-8'))
        print('\nPull configuration request response decrypted message: \n', decrypted_content)
        return decrypted_content

def main():
    config_reader = ConfigReader()
    config = config_reader.fetch_config()

if __name__ == "__main__":
    main()
        </pre>
            </p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible6" :title="dialogTitle6" draggable width="70%"
               style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item timestamp="package.json" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
{
  "name": "dbc-sdk-javascript",
  "version": "1.0.0",
  "description": "dbc sdk for javascript examples",
  "main": "dbc-sdk-javascript.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "keywords": [
    "dbc",
    "sdk",
    "javascript"
  ],
  "author": "",
  "license": "",
  "dependencies": {
    "axios": "^1.8.4",
    "crypto-js": "^4.2.0",
    "node-rsa": "^1.1.1"
  }
}
        </pre>
            </p>
          </el-card>
        </el-timeline-item>

        <el-timeline-item timestamp="dbc-sdk-javascript.js" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
const axios = require('axios');
const NodeRSA = require('node-rsa');
const CryptoJS = require('crypto-js');

class ConfigReader {
    async main() {
        const username = '{Username}';
        const password = '{Password}';
        const serviceName = '{{httpIntegrated.serviceName}}'
        const profileName = '{{httpIntegrated.profileName}}'
        const formats = 'PROP'

        const publicKeyUrl = "http://localhost:30170/h-dbc/api/publicKey";
        let publicKey;
        try {
            const response = await axios.get(publicKeyUrl, {
                auth: {
                    username: username,
                    password: password
                }
            });
            publicKey = response.data.body;
            console.log("\nPublic Key:\n", publicKey);
        } catch (error) {
            console.error("Failed to obtain public key:", error);
            return;
        }

        const aesKey = this.generateRandomString(16);
        const aesIv = this.generateRandomString(16);
        const keyEnc = CryptoJS.enc.Utf8.parse(aesKey);
        const ivEnc = CryptoJS.enc.Utf8.parse(aesIv);

        console.log('\naesKey: %s', aesKey)
        console.log('aesIv: %s', aesIv)

        const jsEncrypt = new NodeRSA(publicKey, 'pkcs8-public');
        jsEncrypt.setOptions({encryptionScheme: 'pkcs1'})
        const rsaKeyEncrypt = jsEncrypt.encrypt(aesKey, 'base64');
        const rsaIvEncrypt = jsEncrypt.encrypt(aesIv, 'base64');

        const app = {
            serviceName: serviceName,
            profileName: profileName,
            type: formats
        };

        const body = {
            key: rsaKeyEncrypt,
            iv: rsaIvEncrypt,
            body: this.encrypt(JSON.stringify(app), keyEnc, ivEnc)
        };

        console.log("\nRequest message:\n", JSON.stringify(body));

        const configUrl = "http://localhost:30170/h-dbc/api/config/list";
        try {
            const response = await axios.post(configUrl, body, {
                headers: {
                    'Content-Type': 'application/json'
                },
                auth: {
                    username: username,
                    password: password
                }
            });
            const encryptedResponse = response.data;
            console.log("\nResponse data, before decryption:\n", encryptedResponse);

            const decryptedResponse = this.decrypt(encryptedResponse, keyEnc, ivEnc);
            console.log("\nResponse data, after decryption:\n", decryptedResponse);
        } catch (error) {
            console.error("Query configuration failed:", error);
        }
    }

    decrypt(word, key, iv) {
        var base64 = CryptoJS.enc.Base64.parse(word);
        var src = CryptoJS.enc.Base64.stringify(base64);
        var decrypt = CryptoJS.AES.decrypt(src, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
        return decryptedStr.toString();
    }

    encrypt(word, key, iv) {
        var srcs = CryptoJS.enc.Utf8.parse(word);
        var encrypted = CryptoJS.AES.encrypt(srcs, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        return CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
    }

    generateRandomString(length) {
        if (length &lt; 1) return '';

        const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        const DIGITS = '0123456789';
        let result = '';

        result += LETTERS[Math.floor(Math.random() * LETTERS.length)];

        const ALL_CHARS = LETTERS + DIGITS;
        for (let i = 1; i &lt; length; i++) {
            result += ALL_CHARS[Math.floor(Math.random() * ALL_CHARS.length)];
        }

        return result;
    }
}

const configReader = new ConfigReader();
configReader.main();
        </pre>
            </p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible7" :title="dialogTitle7" draggable width="70%"
               style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item timestamp="ConfigReader.go" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
package main

import (
    "bytes"
    "crypto/aes"
    "crypto/cipher"
    "crypto/rand"
    "crypto/rsa"
    "crypto/x509"
    "encoding/base64"
    "encoding/json"
    "encoding/pem"
    "fmt"
    "io/ioutil"
    "log"
    "math/big"
    "net/http"
    "strings"
    "time"
)

func randomKeyWithAES(length int) (string, error) {
    const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    result := make([]byte, length)
    for i := range result {
        num, err := rand.Int(rand.Reader, big.NewInt(int64(len(letters))))
        if err != nil {
            return "", err
        }
        result[i] = letters[num.Int64()]
    }
    return string(result), nil
}

func pkcs5Pad(src []byte, blockSize int) []byte {
    padding := blockSize - len(src)%blockSize
    padtext := bytes.Repeat([]byte{byte(padding)}, padding)
    return append(src, padtext...)
}

func pkcs5Unpad(src []byte, blockSize int) ([]byte, error) {
    length := len(src)
    padding := int(src[length-1])
    if padding &gt; blockSize || padding == 0 {
        return nil, fmt.Errorf("invalid padding")
    }
    for i := length - 1; i &gt; length-padding-1; i-- {
        if src[i] != byte(padding) {
            return nil, fmt.Errorf("invalid padding")
        }
    }
    return src[:length-padding], nil
}

func encryptAES(plaintext, key, iv []byte) ([]byte, error) {
    block, err := aes.NewCipher(key)
    if err != nil {
        return nil, err
    }
    plaintext = pkcs5Pad(plaintext, aes.BlockSize)
    ciphertext := make([]byte, len(plaintext))
    stream := cipher.NewCBCEncrypter(block, iv)
    stream.CryptBlocks(ciphertext, plaintext)
    return ciphertext, nil
}

func decryptAES(ciphertext, key, iv []byte) ([]byte, error) {
    block, err := aes.NewCipher(key)
    if err != nil {
        return nil, err
    }
    if len(ciphertext) &lt; aes.BlockSize {
        return nil, fmt.Errorf("ciphertext too short")
    }
    stream := cipher.NewCBCDecrypter(block, iv)
    stream.CryptBlocks(ciphertext, ciphertext)
    return pkcs5Unpad(ciphertext, aes.BlockSize)
}

func encryptRSA(plaintext []byte, publicKey *rsa.PublicKey) ([]byte, error) {
    ciphertext, err := rsa.EncryptPKCS1v15(rand.Reader, publicKey, plaintext)
    if err != nil {
        return nil, err
    }
    return ciphertext, nil
}

func decryptRSA(ciphertext []byte, privateKey *rsa.PrivateKey) ([]byte, error) {
    plaintext, err := rsa.DecryptPKCS1v15(rand.Reader, privateKey, ciphertext)
    if err != nil {
        return nil, err
    }
    return plaintext, nil
}

func getPublicKey(client *http.Client, url, username, password string) (*rsa.PublicKey, error) {
    req, err := http.NewRequest("GET", url, nil)
    if err != nil {
        return nil, err
    }
    req.SetBasicAuth(username, password)
    resp, err := client.Do(req)
    if err != nil {
        return nil, err
    }
    defer resp.Body.Close()
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        return nil, err
    }
    var response map[string]interface{}
    err = json.Unmarshal(body, &response)
    if err != nil {
        return nil, err
    }
    publicKeyStr, ok := response["body"].(string)
    if !ok {
        return nil, fmt.Errorf("invalid response format")
    }
    fmt.Printf("\nPublic key string: \n%s\n", publicKeyStr)

    pemHeader := "-----BEGIN PUBLIC KEY-----\n"
    pemFooter := "\n-----END PUBLIC KEY-----"
    publicKeyPEM := pemHeader + publicKeyStr + pemFooter

    block, _ := pem.Decode([]byte(publicKeyPEM))
    if block == nil {
        return nil, fmt.Errorf("failed to parse PEM block containing the public key")
    }
    pub, err := x509.ParsePKIXPublicKey(block.Bytes)
    if err != nil {
        return nil, err
    }
    publicKey, ok := pub.(*rsa.PublicKey)
    if !ok {
        return nil, fmt.Errorf("public key is not of type *rsa.PublicKey")
    }
    return publicKey, nil
}

func main() {
    username := "{Username}"
    password := "{Password}"
    serviceName := "{{httpIntegrated.serviceName}}"
    profileName := "{{httpIntegrated.profileName}}"
    formats := "PROP"

    client := &http.Client{
        Timeout: 30 * time.Second,
    }

    publicKeyURL := "http://localhost:30170/h-dbc/api/publicKey"
    publicKey, err := getPublicKey(client, publicKeyURL, username, password)
    if err != nil {
        log.Fatalf("Failed to get public key: %v", err)
    }

    aesKey, err := randomKeyWithAES(16)
    if err != nil {
        log.Fatalf("Failed to generate AES key: %v", err)
    }
    aesIv, err := randomKeyWithAES(16)
    if err != nil {
        log.Fatalf("Failed to generate AES IV: %v", err)
    }
    fmt.Printf("\nkey: %s\n", aesKey)
    fmt.Printf("iv: %s\n", aesIv)

    aesKeyBytes := []byte(aesKey)
    aesIvBytes := []byte(aesIv)
    rsaKeyEncrypt, err := encryptRSA(aesKeyBytes, publicKey)
    if err != nil {
        log.Fatalf("Failed to encrypt AES key with RSA: %v", err)
    }
    rsaIvEncrypt, err := encryptRSA(aesIvBytes, publicKey)
    if err != nil {
        log.Fatalf("Failed to encrypt AES IV with RSA: %v", err)
    }
    rsaKeyEncryptBase64 := base64.StdEncoding.EncodeToString(rsaKeyEncrypt)
    rsaIvEncryptBase64 := base64.StdEncoding.EncodeToString(rsaIvEncrypt)

    app := map[string]string{
        "serviceName": serviceName,
        "profileName": profileName,
        "type":        formats,
    }
    appJSON, err := json.Marshal(app)
    if err != nil {
        log.Fatalf("Failed to marshal app JSON: %v", err)
    }

    aesKeyBytes = []byte(aesKey)
    aesIvBytes = []byte(aesIv)
    aesEncrypted, err := encryptAES(appJSON, aesKeyBytes, aesIvBytes)
    if err != nil {
        log.Fatalf("Failed to encrypt app JSON with AES: %v", err)
    }

    body := map[string]string{
        "key":  rsaKeyEncryptBase64,
        "iv":   rsaIvEncryptBase64,
        "body": base64.StdEncoding.EncodeToString(aesEncrypted),
    }
    bodyJSON, err := json.Marshal(body)
    if err != nil {
        log.Fatalf("Failed to marshal body JSON: %v", err)
    }

    fmt.Printf("\nPost body: \n%s\n", bodyJSON)

    configURL := "http://localhost:30170/h-dbc/api/config/list"
    req, err := http.NewRequest("POST", configURL, bytes.NewBuffer(bodyJSON))
    if err != nil {
        log.Fatalf("Failed to create request: %v", err)
    }
    req.Header.Set("Content-Type", "application/json")
    req.SetBasicAuth(username, password)
    resp, err := client.Do(req)
    if err != nil {
        log.Fatalf("Failed to send request: %v", err)
    }
    defer resp.Body.Close()
    respBody, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        log.Fatalf("Failed to read response body: %v", err)
    }
    fmt.Printf("\nResponse data，before decrypt: \n%s\n", respBody)

    respBody = []byte(strings.Trim(string(respBody), `"`))

    ciphertext, err := base64.StdEncoding.DecodeString(string(respBody))
    if err != nil {
        log.Fatalf("Failed to decode Base64 response body: %v", err)
    }

    yamlBytes, err := decryptAES(ciphertext, aesKeyBytes, aesIvBytes)
    if err != nil {
        log.Fatalf("Failed to decrypt response data: %v", err)
    }
    yaml := string(yamlBytes)
    fmt.Printf("\nResponse data，after decrypt: \n%s\n", yaml)
}
        </pre>
            </p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible8" :title="dialogTitle8" draggable width="70%"
               style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item timestamp="ConfigReader.cpp" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
#include &lt;iostream&gt;
#include &lt;string&gt;
#include &lt;sstream&gt;
#include &lt;curl/curl.h&gt;
#include &lt;openssl/evp.h&gt;
#include &lt;openssl/pem.h&gt;
#include &lt;openssl/err.h&gt;
#include &lt;openssl/rand.h&gt;
#include &lt;nlohmann/json.hpp&gt;
#include &lt;random&gt;
#include &lt;algorithm&gt;
#include &lt;map&gt;

using json = nlohmann::json;

std::string generateRandomAlnumString(size_t length) {
    const char alnum[] =
            "0123456789"
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    std::string result;
    result.reserve(length);

    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution&lt;&gt; dis(0, sizeof(alnum) - 2);

    for (size_t i = 0; i &lt; length; ++i) {
        result += alnum[dis(gen)];
    }
    return result;
}

size_t WriteCallback(void* contents, size_t size, size_t nmemb, void* userp) {
    ((std::string*)userp)-&gt;append((char*)contents, size * nmemb);
    return size * nmemb;
}

std::string getPublicKey(const std::string& user, const std::string& password) {
    std::cout &lt;&lt; "Get PublicKey...\n" &lt;&lt; std::endl;
    CURL* curl = curl_easy_init();
    std::string readBuffer;

    if (curl) {
        std::ostringstream oss;
        oss &lt;&lt; "http://" &lt;&lt; user &lt;&lt; ":" &lt;&lt; password &lt;&lt; "@localhost:30170/h-dbc/api/publicKey";
        std::string url = oss.str();
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);

        if (curl_easy_perform(curl) != CURLE_OK) {
            std::cerr &lt;&lt; "HTTP request failed" &lt;&lt; std::endl;
        }
        curl_easy_cleanup(curl);
    }

    if (!readBuffer.empty())  {
        try {
            return json::parse(readBuffer)["body"];
        } catch (...) {
            return "";
        }
    }
    return "";
}

std::string randomKeyWithAES(int length) {
    std::string key(length, 0);
    RAND_bytes((unsigned char*)key.data(),  length);
    return key;
}

std::string base64Encode(const std::string& data) {
    BIO* bio = BIO_new(BIO_f_base64());
    BIO* mem = BIO_new(BIO_s_mem());
    bio = BIO_push(bio, mem);
    BIO_set_flags(bio, BIO_FLAGS_BASE64_NO_NL);
    BIO_write(bio, data.data(),  data.size());
    BIO_flush(bio);

    BUF_MEM* bufferPtr;
    BIO_get_mem_ptr(bio, &bufferPtr);
    std::string result(bufferPtr-&gt;data, bufferPtr-&gt;length);
    BIO_free_all(bio);
    return result;
}

int calcDecodeLength(const char* b64input) {
    int len = strlen(b64input);
    int padding = 0;
    if (b64input[len-1] == '=') padding++;
    if (b64input[len-2] == '=') padding++;
    return (len * 3) / 4 - padding;
}

std::string base64Decode(const std::string& data) {
    BIO* bio = BIO_new_mem_buf(data.c_str(), -1);
    BIO* b64 = BIO_new(BIO_f_base64());
    bio = BIO_push(b64, bio);
    BIO_set_flags(bio, BIO_FLAGS_BASE64_NO_NL);

    int decodeLen = calcDecodeLength(data.c_str());
    std::string result(decodeLen, 0);
    BIO_read(bio, result.data(),  decodeLen);
    BIO_free_all(bio);
    return result;
}

EVP_PKEY* createPublicKey(const std::string& publicKey) {
    // 自动补全PEM格式
    std::string formattedKey = publicKey;
    if (formattedKey.find("-----BEGIN  PUBLIC KEY-----") == std::string::npos) {
        formattedKey = "-----BEGIN PUBLIC KEY-----\n" + formattedKey;
    }
    if (formattedKey.find("-----END  PUBLIC KEY-----") == std::string::npos) {
        formattedKey += "\n-----END PUBLIC KEY-----";
    }

    BIO* keybio = BIO_new_mem_buf(formattedKey.c_str(), -1);
    if (!keybio) {
        std::cerr &lt;&lt; "BIO memory assign failed" &lt;&lt; std::endl;
        return nullptr;
    }

    EVP_PKEY* pkey = PEM_read_bio_PUBKEY(keybio, nullptr, nullptr, nullptr);
    if (!pkey) {
        ERR_print_errors_fp(stderr);
        std::cerr &lt;&lt; "Corrected public key content：\n" &lt;&lt; formattedKey &lt;&lt; std::endl;
    }
    BIO_free(keybio);
    return pkey;
}

std::string rsaEncrypt(const std::string& data, EVP_PKEY* pkey) {
    EVP_PKEY_CTX* ctx = EVP_PKEY_CTX_new(pkey, nullptr);
    if (!ctx || EVP_PKEY_encrypt_init(ctx) &lt;= 0) return "";

    size_t outlen;
    if (EVP_PKEY_encrypt(ctx, nullptr, &outlen, (const unsigned char*)data.data(),  data.size())  &lt;= 0) {
        EVP_PKEY_CTX_free(ctx);
        return "";
    }

    std::string encrypted(outlen, 0);
    if (EVP_PKEY_encrypt(ctx, (unsigned char*)encrypted.data(),  &outlen,
                         (const unsigned char*)data.data(),  data.size())  &lt;= 0) {
        EVP_PKEY_CTX_free(ctx);
        return "";
    }

    EVP_PKEY_CTX_free(ctx);
    return base64Encode(encrypted);
}

std::string aesEncrypt(const std::string& data, const std::string& key, const std::string& iv) {
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx || EVP_EncryptInit_ex(ctx, EVP_aes_128_cbc(), nullptr,
                                   (const unsigned char*)key.data(),
                                   (const unsigned char*)iv.data())  != 1) {
        EVP_CIPHER_CTX_free(ctx);
        return "";
    }

    int block_size = EVP_CIPHER_CTX_get_block_size(ctx);
    int outlen = data.size()  + block_size;
    std::string encrypted(outlen, 0);

    int len;
    EVP_EncryptUpdate(ctx, (unsigned char*)encrypted.data(),  &len,
                      (const unsigned char*)data.data(),  data.size());
    int final_len;
    EVP_EncryptFinal_ex(ctx, (unsigned char*)encrypted.data()  + len, &final_len);
    encrypted.resize(len  + final_len);
    EVP_CIPHER_CTX_free(ctx);
    return base64Encode(encrypted);
}

std::string aesDecrypt(const std::string& data, const std::string& key, const std::string& iv) {
    std::string decoded = base64Decode(data);
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx || EVP_DecryptInit_ex(ctx, EVP_aes_128_cbc(), nullptr,
                                   (const unsigned char*)key.data(),
                                   (const unsigned char*)iv.data())  != 1) {
        EVP_CIPHER_CTX_free(ctx);
        return "";
    }

    int block_size = EVP_CIPHER_CTX_get_block_size(ctx);
    int outlen = decoded.size()  + block_size;
    std::string decrypted(outlen, 0);

    int len;
    EVP_DecryptUpdate(ctx, (unsigned char*)decrypted.data(),  &len,
                      (const unsigned char*)decoded.data(),  decoded.size());
    int final_len;
    EVP_DecryptFinal_ex(ctx, (unsigned char*)decrypted.data()  + len, &final_len);
    decrypted.resize(len  + final_len);
    EVP_CIPHER_CTX_free(ctx);
    return decrypted;
}

int main() {

    std::string user = "{Username}";
    std::string password = "{Password}";
    std::string serviceName = "{{httpIntegrated.serviceName}}";
    std::string profileName = "{{httpIntegrated.profileName}}";
    std::string formats = "YAML";

    std::string publicKey = getPublicKey(user, password);
    if (publicKey.empty())  {
        std::cerr &lt;&lt; "Error: The public key obtained is empty" &lt;&lt; std::endl;
        return 1;
    } else {
        std::cout &lt;&lt; "Get the public key: \n" &lt;&lt; publicKey &lt;&lt; "..." &lt;&lt; std::endl;
    }

    std::string aesKey = generateRandomAlnumString(16);
    std::string aesIv = generateRandomAlnumString(16);
    // AES密钥生成后
    std::cout &lt;&lt; "\nGenerate AES key: " &lt;&lt; aesKey &lt;&lt; std::endl;
    std::cout &lt;&lt; "Generate AES IV: " &lt;&lt; aesIv &lt;&lt; std::endl;

    EVP_PKEY* pkey = createPublicKey(publicKey);
    if (!pkey) return 1;

    std::string rsaEncryptKey = rsaEncrypt(aesKey, pkey);
    std::string rsaEncryptIv = rsaEncrypt(aesIv, pkey);
    std::cout &lt;&lt; "\nAfter AES key RSA encryption: " &lt;&lt; rsaEncryptKey &lt;&lt; std::endl;
    std::cout &lt;&lt; "After AES IV RSA encryption: " &lt;&lt; rsaEncryptIv &lt;&lt; std::endl;

    std::map&lt;std::string, std::string&gt; request_map = {
            {"serviceName", serviceName},
            {"profileName", profileName},
            {"type", formats}
    };

    json request = request_map;
    std::string bodyEncrypt = aesEncrypt(request.dump(),  aesKey, aesIv);

    json payload = {
            {"key", rsaEncryptKey},
            {"iv", rsaEncryptIv},
            {"body", bodyEncrypt}
    };
    EVP_PKEY_free(pkey);

    CURL* curl = curl_easy_init();
    std::string response;
    if (curl) {
        std::ostringstream oss;
        oss &lt;&lt; "http://" &lt;&lt; user &lt;&lt; ":" &lt;&lt; password &lt;&lt; "@localhost:30170/h-dbc/api/config/list";
        std::string url = oss.str();
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, payload.dump().c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);

        struct curl_slist* headers = nullptr;
        headers = curl_slist_append(headers, "Content-Type: application/json; charset=utf-8");
        headers = curl_slist_append(headers, "Accept: application/json");

        std::string jsonPayload = payload.dump();
        std::cout &lt;&lt; "\nJSON sent: \n" &lt;&lt; jsonPayload &lt;&lt; std::endl;

        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, jsonPayload.c_str());
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, jsonPayload.length());


        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        curl_easy_perform(curl);
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
    }

    if (!response.empty())  {
        std::cout &lt;&lt; "\nResponse: \n" &lt;&lt; response &lt;&lt; std::endl;
        std::cout &lt;&lt; "\nDecrypted: \n" &lt;&lt; aesDecrypt(response, aesKey, aesIv) &lt;&lt; std::endl;
    }
    return 0;
}
        </pre>
            </p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible9" :title="dialogTitle9" draggable width="70%"
               style="margin-top: 20px;height: 90%;overflow-y: auto">
      <el-timeline>
        <el-timeline-item timestamp="Maven Dependency" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
&lt;dependency&gt;
  &lt;groupId&gt;com.github.hbq969&lt;/groupId&gt;
  &lt;artifactId>spring-cloud-starter-hdbc-config&lt;/artifactId&gt;
  &lt;version>latest version&lt;/version&gt;
&lt;/dependency&gt;
        </pre>
            </p>
          </el-card>
        </el-timeline-item>

        <el-timeline-item :timestamp="httpIntegrated.profileName=='default'?'bootstrap.yml':'bootstrap-'+httpIntegrated.profileName+'.yml'" placement="top">
          <el-card>
            <p>
        <pre style="font-size: 0.8em">
spring:
  cloud:
    config:
      h-dbc:
        enabled: true
        dbc-key: "h-dbc"
        service-name: "{{httpIntegrated.serviceName}}"
        profile-name: "{{httpIntegrated.profileName}}"
        api:
          charset: "utf-8"
          url: "http://localhost:30170/h-dbc"
          basic-auth:
            username: "api authentication account"
            password: "api authentication password"
        </pre>
            </p>
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
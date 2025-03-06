<script lang="ts" setup xmlns="http://www.w3.org/1999/html">
import {
   ArrowLeft, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, onBeforeUnmount} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import router from "@/router";
import * as monaco from 'monaco-editor';
import {ElMessageBox} from 'element-plus'
import {getLangData} from "@/i18n/locale";

const langData = getLangData()

// 获取编辑器容器的引用
const editorContainer = ref<HTMLElement | null>(null);
const diffEditorContainer = ref<HTMLElement | null>(null);
let editor: monaco.editor.IStandaloneCodeEditor | null = null;
let diffEditor: monaco.editor.IStandaloneCodeEditor | null = null;
let originalModel = monaco.editor.createModel('', 'yaml');
let modifiedModel = monaco.editor.createModel('', 'yaml');

onMounted(() => {
  queryConfigFile()
});

// 销毁编辑器
onBeforeUnmount(() => {
  if (editor) {
    editor.dispose();
  }
});

const goConfigProfile = () => {
  let query = router.currentRoute.value.query
  delete query.profileName
  delete query.profileDesc
  router.push({
    path: '/config/profile',
    query: query
  })
}

const data = reactive({
  total: 0,
  configList: [],
  configFile: {
    fileContent: ''
  }
})

const queryConfigFile = () => {
  axios({
    url: '/config/file/info',
    method: 'post',
    data: router.currentRoute.value.query
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      if (res.data.body && res.data.body.fileContent) {
        data.configFile = res.data.body
        initialEditor()
      } else {
        msg(langData.configFileNoConfigData, 'info')
        initialEditor()
      }
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
      initialEditor()
    }
  }).catch((err: Error) => {
    console.log('', err)
    msg(langData.axiosRequestErr, 'error')
  })
}

const initialEditor = () => {
  if (editorContainer.value && diffEditorContainer.value) {
    // 创建普通编辑器
    editor = monaco.editor.create(editorContainer.value, {
      value: '',
      language: 'yaml',
      theme: 'vs-dark',
      fontSize: 10,
      wordWrap: 'on'
    });

    editor?.setValue(data.configFile.fileContent ? data.configFile.fileContent : '')

    // 创建 DiffEditor
    diffEditor = monaco.editor.createDiffEditor(diffEditorContainer.value, {
      theme: 'vs-dark',
      readOnly: true,
      fontSize: 10,
      wordWrap: 'on'
    });

    // 设置初始的原始和修改后的内容
    originalModel = monaco.editor.createModel(data.configFile.fileContent, 'yaml');
    modifiedModel = monaco.editor.createModel(data.configFile.fileContent, 'yaml');
    diffEditor.setModel({
      original: originalModel,
      modified: modifiedModel,
    });

    // 监听编辑器的内容变化
    editor.onDidChangeModelContent(() => {
      const modifiedValue = editor.getValue();
      modifiedModel.setValue(modifiedValue);
      let originalContent = originalModel.getValue()
      if (!originalContent || originalContent.trim() == '') {
        originalModel.setValue(modifiedValue)
      }
    });
  }
}
const backup = ref('N')
const saveConfigFile = () => {
  let content = editor?.getValue()
  if (!content || content.trim() == '') {
    ElMessageBox.alert(langData.configFileMsgBoxAlert, langData.msgBoxTitle, {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  let form = router.currentRoute.value.query
  form.fileContent = content
  form.backup = backup.value
  axios({
    url: '/config/file',
    method: 'post',
    data: form,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body,'success')
      originalModel.setValue(form.fileContent)
      modifiedModel.setValue(form.fileContent)
    } else {
      let content = res.config.baseURL+res.config.url+': '+res.data.errorMessage;
      msg(content, "warning")
    }
  }).catch((err: Error) => {
    console.log('',err)
    msg(langData.axiosRequestErr, 'error')
  });
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
    <el-page-header :icon="ArrowLeft" @back="goConfigProfile">
      <template #content>
        <span class="text-large font-600 mr-3" style="font-size: 15px"> {{ langData.configProfileHeaderCreator }}：{{ router.currentRoute.value.query.username }}，{{langData.configProfileHeaderServiceName}}: {{
            router.currentRoute.value.query.serviceName
          }}，{{ langData.configProfileProfileName }}: {{
            router.currentRoute.value.query.profileName
          }}({{ router.currentRoute.value.query.profileDesc }}) </span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0"></el-divider>
    <div style="display: flex; width: 100%">
      <div id="editorContainer" ref="editorContainer" style="flex:1; height: 500px;width: 50%"></div>
      <div id="diffEditorContainer" ref="diffEditorContainer" style="flex:1; height: 500px;width: 50%; margin-left: 5px"></div>
    </div>
    <el-form-item style="margin-top: 10px">
      <el-button type="primary" size="small" @click="saveConfigFile()">{{langData.btnSave}}</el-button>
      <el-form-item :label="langData.configProfileIfBackup" prop="backup" style="margin-left: 10px">
        <el-switch
            v-model="backup"
            inline-prompt
            :active-text="langData.switchYes"
            :inactive-text="langData.switchNo"
            active-value="Y"
            inactive-value="N"
        />
      </el-form-item>
      <span style="margin-left: 5px;color: red">* {{langData.configFileValidateSupportYaml}}</span>
    </el-form-item>
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
<script lang="ts" setup xmlns="http://www.w3.org/1999/html">
import {
  Edit, ArrowLeft, UploadFilled, Delete
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, onBeforeUnmount} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import router from "@/router";
import * as monaco from 'monaco-editor';

// 获取编辑器容器的引用
const editorContainer = ref<HTMLElement | null>(null);
const diffEditorContainer = ref<HTMLElement | null>(null);
let editor: monaco.editor.IStandaloneCodeEditor | null = null;
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
      if (res.data.body) {
        data.configFile = res.data.body
        initialEditor()
      } else {
        msg('无配置数据', 'info')
        initialEditor()
      }
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const initialEditor = () => {
  if (editorContainer.value && diffEditorContainer.value) {
    // 创建普通编辑器
    editor = monaco.editor.create(editorContainer.value, {
      value: '',
      language: 'yaml',
      theme: 'vs-dark',
    });

    editor?.setValue(data.configFile.fileContent)

    // 创建 DiffEditor
    const diffEditor = monaco.editor.createDiffEditor(diffEditorContainer.value, {
      theme: 'vs-dark',
      readOnly: true,
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
      if(!originalContent || originalContent.trim()==''){
        originalModel.setValue(modifiedValue)
      }
    });
  }
}

const saveConfigFile = () => {
  let content = editor?.getValue()
  if (!content || content.trim() == '') {
    alert('配置文件内容不能为空')
    return
  }
  let form = router.currentRoute.value.query
  form.fileContent = content
  axios({
    url: '/config/file',
    method: 'post',
    data: form,
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      originalModel.setValue(form.fileContent)
      modifiedModel.setValue(form.fileContent)
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
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
        <span class="text-large font-600 mr-3"> 配置管理，创建者：{{ router.currentRoute.value.query.username }}，服务: {{
            router.currentRoute.value.query.serviceName
          }}，环境: {{
            router.currentRoute.value.query.profileName
          }}({{ router.currentRoute.value.query.profileDesc }}) </span>
      </template>
    </el-page-header>
    <el-divider content-position="left"></el-divider>
    <div ref="editorContainer" style="height: 300px; width: 90%;"></div>
    <div ref="diffEditorContainer" style="height: 300px; width: 92%; margin-top: 20px;"></div>
    <el-form-item style="margin-top: 10px">
      <el-button type="primary" size="small" @click="saveConfigFile()">保存配置</el-button>
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
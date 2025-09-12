<script lang="ts" setup>
import {
  Edit, ArrowLeft
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject, onBeforeUnmount} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import router from "@/router";
import * as monaco from 'monaco-editor';
import {ITextModel} from "monaco-editor";
import {getLangData} from "@/i18n/locale";

const langData = getLangData()

const diffEditorContainer = ref<HTMLElement | null>(null);
let diffEditor: monaco.editor.IStandaloneCodeEditor | null = null;
let profile1Model = monaco.editor.createModel('', 'yaml');
let profile2Model = monaco.editor.createModel('', 'yaml');

const form = reactive({
  profileName: '',
})

onMounted(() => {
  initialEditor()
  queryConfigFile()
});

// 销毁编辑器
onBeforeUnmount(() => {
  if (diffEditor) {
    diffEditor.dispose();
  }
});

const initialEditor = () => {
  diffEditor = monaco.editor.createDiffEditor(diffEditorContainer.value, {
    theme: 'vs-dark',
    readOnly: true,
    fontSize: 10,
    wordWrap: 'on'
  });
  profile1Model = monaco.editor.createModel('', 'yaml');
  profile2Model = monaco.editor.createModel('', 'yaml');
  diffEditor.setModel({
    original: profile1Model,
    modified: profile2Model,
  });
}

const data = reactive({
  profileList: []
})

const queryConfigFile = () => {
  axios({
    url: '/config/file/info/currentAndBackup',
    method: 'post',
    data: {
      current: {
        username: router.currentRoute.value.query.username,
        serviceId: router.currentRoute.value.query.serviceId,
        profileName: router.currentRoute.value.query.profileName
      },
      backupId: router.currentRoute.value.query.id
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      let file = {currentFileContent: '', backupFileContent: ''};
      file = res.data.body
      profile1Model.setValue(file.currentFileContent)
      profile2Model.setValue(file.backupFileContent)
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
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
    <el-page-header :icon="ArrowLeft" @back="router.back()">
      <template #content>
        <span class="text-large font-600 mr-3" style="font-size: 15px"> {{ langData.configCompareHeaderTitle }} </span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0"></el-divider>
    <el-row>
      <el-col :span="12">
        <el-link :underline="false" type="success">{{langData.currentFileContentTitle}}</el-link>
      </el-col>
      <el-col :span="12">
        <el-link :underline="false" type="warning">{{langData.willRecoveryFileContentTitle}}</el-link>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <div ref="diffEditorContainer" style="height: 500px; width: 100%; margin-top: 10px;"></div>
      </el-col>
    </el-row>
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
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
  profile1: 'default',
  profile2: 'dev'
})

onMounted(() => {
  initialEditor()
  queryAllProfileList()
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
      if (data.profileList.length != 0) {
        let profile = data.profileList[0]
        form.profile1 = profile.profileName
        form.profile2 = profile.profileName
        queryConfigFile(profile1Model,form.profile1)
        queryConfigFile(profile2Model,form.profile2)
      }
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('',err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const queryConfigFile = (model: ITextModel, profileName) => {
  axios({
    url: '/config/file/info',
    method: 'post',
    data: {
      username: router.currentRoute.value.query.username,
      serviceId: router.currentRoute.value.query.serviceId,
      profileName: profileName
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      let file={fileContent:''};
      if (res.data.body && res.data.body.fileContent) {
        file = res.data.body
      } else {
        msg(langData.configFileNoConfigData, 'info')
      }
      model.setValue(file.fileContent)
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: any) => {
    console.log('', err)
    msg(err?.response.data.errorMessage, 'error')
  })
}

const profile1Change=()=>{
  queryConfigFile(profile1Model,form.profile1)
}

const profile2Change=()=>{
  queryConfigFile(profile2Model,form.profile2)
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
        <span class="text-large font-600 mr-3" style="font-size: 15px"> {{langData.configCompareHeaderTitle}} </span>
      </template>
    </el-page-header>
    <el-divider content-position="left" style="margin: 10px 0"></el-divider>
    <el-row>
      <el-col :span="12">
        <el-radio-group v-model="form.profile1" size="small" @change="profile1Change">
          <el-radio-button :label="profile.profileName" :value="profile.profileName"
                           v-for="profile in data.profileList"/>
        </el-radio-group>
      </el-col>
      <el-col :span="12">
        <el-radio-group v-model="form.profile2" size="small" @change="profile2Change">
          <el-radio-button :label="profile.profileName" :value="profile.profileName"
                           v-for="profile in data.profileList"/>
        </el-radio-group>
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
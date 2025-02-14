<script lang="ts" setup>
import {
  Edit, ArrowLeft, Delete, Grid, DocumentCopy
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed, provide, inject} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
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
      <el-card style="max-width: 250px;height: 220px;" v-for="(source,index) in data.profileList">
        <template #header="scope">
          <div class="card-header" style="display: flex; align-items: center; justify-content: space-between">
            <div style="margin-top: 5px;text-align: center;flex: 1;display: flex; justify-content: left">
              <component is="MenuIcon"/>
            </div>
            <div style="flex: 1; display: flex; justify-content: flex-end">
              <el-popconfirm title="确认清空此环境配置?" confirm-button-type="danger" @confirm="deleteConfig(source)">
                <template #reference>
                  <el-button type="info" size="small" circle :icon="Delete"/>
                </template>
              </el-popconfirm>
              <el-badge :value="0" style="margin-left: 12px" type="danger" max="500" :hidden="true">
                <el-button type="primary" size="small" circle :icon="DocumentCopy" @click="goConfigFile(source)"/>
              </el-badge>
              <el-badge :value="source.configNum" style="margin-left: 12px" type="danger" max="500"
                        :hidden="source.configNum==0">
                <el-button type="success" size="small" circle :icon="Grid" @click="goConfigList(source)"/>
              </el-badge>
            </div>
          </div>
        </template>
        <template #default="scope">
          <el-form size="small" label-position="right" inline-message :inline="false" label-width="100px"
                   style="width:200px">
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
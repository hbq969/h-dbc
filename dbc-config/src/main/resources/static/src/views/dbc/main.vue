<script lang="ts" setup>
import type Node from 'element-plus/es/components/tree/src/model/node'
import {ArrowRight} from '@element-plus/icons-vue'
import {
  Document,
  DocumentCopy,
  Expand,
  Download,
  Upload,
  Search,
  Clock,
  Plus,
  Folder,
  Delete,
  WarningFilled,
  UploadFilled,
  HomeFilled,
  Refresh,
  Edit,
  SwitchButton
} from '@element-plus/icons-vue'
import {UploadInstance, UploadProps, UploadFile, FormInstance, FormRules, ElMessage} from 'element-plus'
import {ref, reactive, computed, onMounted} from 'vue'
import router from '@/router/index'
import axios from '@/network/index'
import {msg, downFile} from '@/utils/Utils'

const formLabelWidth = ref('140px')
const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const form = reactive({
});

const data = reactive({
});

// 查询字典列表
const test = () => {
  axios({
    url: '/hello',
    method: 'get',
    data: form
  }).then((res: any) => {
    alert(res.data)
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

onMounted(()=>{
  test()
})

</script>

<template>
  <div class="container">

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
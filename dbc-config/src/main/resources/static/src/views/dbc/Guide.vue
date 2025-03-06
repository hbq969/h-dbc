<script lang="ts" setup>
import {ref, reactive, onMounted, computed} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/default.css'; // 引入 highlight.js 样式
import mk from 'markdown-it-katex';

const showContent = ref('')
const language = sessionStorage.getItem('h-sm-lang') || 'zh-CN'

const md = new MarkdownIt({
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre><code class="hljs">${hljs.highlight(str, {language: lang}).value}</code></pre>`;
      } catch (__) {
      }
    }
    return `<pre><code class="hljs">${md.utils.escapeHtml(str)}</code></pre>`;
  },
}).use(mk);
const renderedHTML = computed(() => {
  return md.render(showContent.value);
});

onMounted(() => {
  axios({
    url: '/md/README_' + language + '.md',
    method: 'get',
  }).then((res: any) => {
    showContent.value = res.data
  }).catch((err: Error) => {
    console.log('', err)
    msg('请求异常', 'error')
  })
});

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
    <div v-html="renderedHTML" class="markdown-content"></div>
  </div>
</template>

<style scoped>
.container {
  flex-grow: 1;
  padding: 20px 2%;
  overflow: auto;
  width: 96%;
}

.markdown-content {
  padding: 10px;
  border: 0px solid #ccc;
  border-radius: 4px;
  background-color: #f9f9f9;
  font-family: "Times New Roman", Times, serif;
  line-height: 1.5;
  width: 60%; /* 可以调整为适合你设计的宽度 */
  max-width: 100%; /* 让容器在需要时自适应更宽的内容 */
  word-wrap: break-word; /* 确保长单词能断开换行 */
  white-space: normal; /* 允许自动换行 */
  overflow-wrap: break-word; /* 防止长单词溢出 */
}
</style>

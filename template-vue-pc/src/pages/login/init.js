import Vue from 'vue';
import { LoadingBar, Message, Notice } from 'view-design';

import '@/../public/static-p/js/urls';
import '@/../public/static-p/js/utils';
import '@/../public/static-p/lib/axios.rest';

// 定义全局变量: Message, Notice，为了能够在 Dao 中使用
window.LoadingBar = LoadingBar;
window.Message    = Message;
window.Notice     = Notice;

// 其他
Vue.prototype.window = window;

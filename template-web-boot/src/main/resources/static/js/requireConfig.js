require.config({
    paths: {
        jquery    : '//cdn.bootcss.com/jquery/1.9.1/jquery.min',
        layer     : '//cdn.staticfile.org/layer/2.3/layer',
        vue       : '//cdn.staticfile.org/vue/2.0.3/vue',
        semanticUi: '//cdn.staticfile.org/semantic-ui/2.2.7/semantic.min',
        ztree     : '//cdn.staticfile.org/zTree.v3/3.5.28/js/jquery.ztree.all.min',
        rest      : '/lib/jquery.rest',
        urls      : '/js/urls',
        util      : '/js/util'
    },
    shim: {
        layer: {
            deps: ['jquery', 'css!//cdn.staticfile.org/layer/2.3/skin/layer.css']
        },
        semanticUi: {
            deps: ['jquery']
        },
        ztree: {
            deps: ['jquery',
                   'css!//cdn.staticfile.org/font-awesome/4.7.0/css/font-awesome.min.css',
                   'css!//cdn.staticfile.org/zTree.v3/3.5.28/css/awesomeStyle/awesome.min.css',
                   'css!/css/ztree-awesome-custom.css']
        },
        rest: {
            deps: ['jquery']
        },
        util: {
            deps: ['jquery']
        }
    },
    map: {
        '*': {
            css: '/lib/css.min.js'
        }
    }
});

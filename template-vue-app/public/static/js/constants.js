const QuestionTypes = {
    BASE_TYPE_SINGLE_CHOICE     : 1, // 单选题单选题
    BASE_TYPE_MULTI_CHOICE      : 2, // 多选题多选题
    BASE_TYPE_TFNG              : 3, // 判断题判断题: true(是), false(否), not given(未提及)
    BASE_TYPE_ESSAY_QUESTION    : 4, // 问答题问答题
    BASE_TYPE_FILL_IN_THE_BLANK : 5, // 填空题填空题
    BASE_TYPE_COMPREHENSION     : 6, // 复合题
    BASE_TYPE_HOMEWORK          : 7, // 作业题
    BASE_TYPE_DESCRIPTION       : 8, // 描述
};

// 教学阶段
const PHASES = ['高中', '初中', '小学'];

// 学科
const SUBJECTS = ['语文', '数学', '英语', '物理', '化学', '生物', '地理', '政治', '历史'];

// 省
const PROVINCES = ['北京', '上海', '天津', '重庆', '河北', '辽宁', '黑龙江', '吉林', '山东', '山西', '安徽', '浙江',
    '江苏', '江西', '广东', '福建', '海南', '河南', '湖北', '湖南', '四川', '云南', '贵州', '陕西', '甘肃',
    '青海', '内蒙古', '广西', '西藏', '新疆', '香港', '澳门', '台湾',
];

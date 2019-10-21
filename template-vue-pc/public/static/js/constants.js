const QUESTION_TYPES = {
    BASE_TYPE_SINGLE_CHOICE    : 1, // 单选题单选题
    BASE_TYPE_MULTI_CHOICE     : 2, // 多选题多选题
    BASE_TYPE_TFNG             : 3, // 判断题判断题: true(是), false(否), not given(未提及)
    BASE_TYPE_ESSAY_QUESTION   : 4, // 问答题问答题
    BASE_TYPE_FILL_IN_THE_BLANK: 5, // 填空题填空题
    BASE_TYPE_COMPLEX          : 6, // 复合题
    BASE_TYPE_HOMEWORK         : 7, // 作业题
    BASE_TYPE_DESCRIPTION      : 8, // 描述
};

// 项目的状态
const PROJECT_STATUS = [
    { value: 0, color: '#aaa',    label: '未提交', img: '' },
    { value: 1, color: 'warning', label: '待审批', img: '' },
    { value: 2, color: 'error',   label: '未通过', img: '/static/img/project-unpass.png' },
    { value: 3, color: 'success', label: '已通过', img: '/static/img/project-pass.png' },
];

// 项目的状态值
const PROJECT_STATUS_VALUE = {
    STATUS_NON_SUBMITTED_TO_APPROVE: 0, // 未提交: 编辑后，但是未提交审批
    STATUS_WAITING_FOR_APPROVING   : 1, // 待审批: 提交等待审批
    STATUS_UNAPPROVED              : 2, // 未通过: 审批未通过
    STATUS_APPROVED                : 3, // 已通过: 审批已通过
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

// 角色
const ROLES = [
    { name: '学员', value: 'ROLE_STUDENT' },
    { name: '班主任', value: 'ROLE_FORM_TEACHER' },
    { name: '项目审批员', value: 'ROLE_PROJECT_APPROVER' },
    { name: '机构管理员', value: 'ROLE_ADMIN_ORG' }
];

// 性别
const GENDERS = [
    { name: '未设置', value: 0 },
    { name: '男', value: 1 },
    { name: '女', value: 2 },
];

// 身份证件类型
const ID_CARD_TYPES = [
    { name: '护照', value: 1 },
    { name: '户口簿', value: 2 },
    { name: '其他', value: 3 },
    { name: '居民身份证', value: 4 },
    { name: '军官证', value: 5 },
    { name: '士兵证', value: 6 },
    { name: '文职干部证', value: 7 },
    { name: '部队离退休证', value: 8 },
    { name: '香港特区护照/身份证明', value: 9 },
    { name: '澳门特区护照/身份证明', value: 10 },
    { name: '台湾居民来往大陆通行证', value: 11 },
    { name: '境外永久居住证', value: 12 },
    { name: '涉密证件', value: 13 },
    { name: '手机号码涉密', value: 14 },
];

// 字典类型
const DICTS = ['专业领域', '委托合作单位性质', '办学性质', '招生类型', '培训对象类别', '新闻公告'];

// 表单项模板
// label     : Form 中的 label
// name      : Form 中的 input 的 name
// customized: 模板项为 false，用户自定义的项为 true，在表单编辑器中使用
// required  : Form 中此项是否必填
// span      : Form 中占据的列数
// options   : type 为 select 时的下拉选项
FORM_TEMPLATE_FIELDS = [
    { label: '姓名', name: 'nickname', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '账号', name: 'username', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '性别', name: 'gender',   type: 'select', customized: false, required: false, span: 1, options: ['未选', '女', '男'] },
    { label: '民族', name: 'nation',   type: 'select', customized: false, required: false, span: 1, options: [
        '汉族', '满族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '侗族', '瑶族', '白族', '土家族',
        '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族', '佤族', '畲族', '高山族', '拉祜族', '水族', '东乡族', '纳西族', '景颇族',
        '柯尔克孜族', '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族', '毛南族', '仡佬族', '锡伯族', '阿昌族', '普米族',
        '朝鲜族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族', '德昂族', '保安族', '裕固族', '京族',
        '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族', '珞巴族', '基诺族'
    ] },
    { label: '邮箱', name: 'email',  type: 'email',  customized: false, required: false, span: 1, options: [] },
    { label: '手机', name: 'mobile', type: 'number', customized: false, required: false, span: 1, options: [] },
    { label: '职务', name: 'title',  type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: 'QQ',  name: 'qq',     type: 'number', customized: false, required: false, span: 1, options: [] },
    { label: '单位性质', name: 'workUnitType', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '工作单位', name: 'workUnit',     type: 'string', customized: false, required: false, span: 1, options: [] },

    { label: '证件类型', name: 'idCardType',   type: 'select', customized: false, required: false, span: 1, options: [
        '居民身份证', '军官证', '士兵证', '护照', '户口簿', '文职干部证', '部队离退休证', '香港特区护照/身份证明',
        '澳门特区护照/身份证明', '台湾居民来往大陆通行证', '境外永久居住证', '涉密证件', '手机号码涉密', '其他'
    ] },
    { label: '证件号码', name: 'idCardNumber', type: 'string', customized: false, required: false, span: 1, options: [] },
    { label: '最高学历', name: 'educationBg',  type: 'select', customized: false, required: false, span: 1, options: [
        '大专', '本科', '研究生', '高中及高中以下'
    ] },

    { label: '联系地址', name: 'address',  type: 'address', customized: false, required: false, span: 2, options: [] },
    { label: '出生年月', name: 'birthday', type: 'date',    customized: false, required: false, span: 1, options: [] },
];

// 表单属性的类型
FORM_FIELD_TYPES = [
    { value: 'string',  name: '字符串' },
    { value: 'number',  name: '数字' },
    { value: 'email',   name: '邮箱' },
    { value: 'date',    name: '日期' },
    { value: 'address', name: '地址' },
    { value: 'select',  name: '下拉框' },
];

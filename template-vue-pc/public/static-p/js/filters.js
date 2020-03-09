/**
 * 在此定义 Vue 全局的过滤器
 *
 * 1. 在 main.js 中安装全局过滤器
 *    import filters from '@/../public/static-p/js/filters';
 *
 *    Object.keys(filters).forEach(key => {
 *        Vue.filter(key, filters[key]);
 *    });
 * 2. 使用过滤器: {{ date | formatDate }}
 */

/**
 * 格式化时间的过滤器
 *
 * @param  {Date} date 时间对象
 * @return {String} 返回日期格式化得到的字符串
 */
const formatDate = function(date) {
    // 1. 如果日期不存在，返回 ---
    // 2. 如果日期是字符串格式的，先转为日期对象
    // 3. 格式化日期

    if (!date) {
        return '---';
    }

    return dayjs(date).format('YYYY-MM-DD'); // 使用 dayjs 格式化时间
};

/**
 * 返回角色对应的名字
 *
 * @param  {String} role 角色
 * @return {String} 返回角色的名字
 */
const roleName = function(role) {
    return ROLES.filter(r => r.value === role).map(r => r.name).join('');
};

/**
 * 根据性别的值获取对应的名字
 *
 * @param  {Integer} gender 性别的值，查看 constants.js 中 GENDERS 的定义
 * @return {String} 返回性别的名字
 */
const genderName = function(gender) {
    return GENDERS.filter(t => t.value === gender).map(t => t.name).join('');
};

/**
 * 根据身份信息类型值获取对应的名字
 *
 * @param  {Integer} value 身份信息的类型值
 * @return {String} 返回身份类型名字
 */
const idCardName = function(value) {
    return ID_CARD_TYPES.filter(t => t.value === value).map(t => t.name).join('');
};

/**
 * 把文件仓库的 URL '/file/repo/2018-06-19/293591971581788160.docx'
 * 转换为下载的 URL '/file/download/2018-06-19/293591971581788160.docx'
 *
 * @param  {String} repoUrl 文件仓库的 URL
 * @return {String} 返回下载使用的 URL
 */
const repoUrlToDownloadUrl = function(repoUrl) {
    if (repoUrl) {
        return repoUrl.replace('/file/repo', '/file/download');
    }

    return '';
};

/**
 * 去掉字符串 str 两边的空白，如果 str 为空白字符串，则返回 defaultValue
 *
 * @param {String} str 要 trim 的字符串
 * @param {String} defaultValue trim 为空白字符串时的默认值
 */
const trim = function(str, defaultValue = '') {
    const text = str && str.trim();
    return text || defaultValue;
};

export default {
    formatDate,
    roleName,
    genderName,
    idCardName,
    repoUrlToDownloadUrl,
    trim,
};

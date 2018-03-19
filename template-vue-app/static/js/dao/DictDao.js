/**
 * 字典的数据访问类
 */
export default class DictDao {
    /**
     * 向服务器请求字典的分类
     *
     * @param  {Function} callback 请求成功的回调函数，参数为字典分类的数组，如 ["学段", "课件"]
     * @return 无返回值
     */
    static findTypes(callback) {
        $.rest.get({url: Urls.API_DICT_TYPES, success: (result) => {
            callback(result.data);
        }});
    }

    /**
     * 查找指定类型下的字典，如果 type 为空或者 null，则查找所有的字典
     *
     * @param  {String}   type             字典分类
     * @param  {Integer}  pageNumber       页码
     * @param  {Integer}  pageSize         数量
     * @param  {Function} successCallback  请求成功的回调函数，参数为字典的数组
     * @param  {Function} completeCallback 请求完成的回调函数
     * @return 无返回值
     */
    static findByType(type, pageNumber, pageSize, successCallback, completeCallback) {
        $.rest.get({url: Urls.API_DICTS, data: {type, pageNumber, pageSize},
            success: (result) => {
                successCallback(result.data);
            },
            complete: () => {
                completeCallback && completeCallback();
            }
        });
    }
}

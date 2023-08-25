import { getCurrentInstance } from 'vue';

/**
 * 获取 mitt() 创建的对象，用作于 EventBus：
 * - emitter = useEmitter()
 * - 发送事件: emitter.emit('event-name', params)
 * - 监听事件: emitter.on('event-name', handler)
 * - 取消监听: emitter.off('event-name', handler)
 *
 * 使用说明:
 * - https://github.com/developit/mitt
 * - https://stackoverflow.com/questions/63471824/vue-js-3-event-bus
 *
 * @returns 返回 mitt() 创建的对象。
 */
export default function useEmitter() {
    const instance = getCurrentInstance();
    const emmiter = instance?.appContext.config.globalProperties.emitter;

    return emmiter;
}

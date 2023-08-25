/**
 * 复制功能。
 *
 * 使用方法:
 * 1. 导入: import { useCopy } from '@/components/Copy';
 * 2. 初始化: const { copy } = useCopy();
 * 3. 使用复制: copy(item.node!.title);
 */
import useClipboard from 'vue-clipboard3'

export function useCopy() {
    const { toClipboard } = useClipboard()

    async function copy(msg: string) {
        try {
            // 复制
            await toClipboard(msg)
            // 复制成功
            alert('复制成功');
        } catch (e) {
            // 复制失败
        }
    }

    return { copy }
}

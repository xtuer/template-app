import { ref } from 'vue';
import { CATALOG, SCHEMA, TABLE, VIEW } from '@/static/types';
import type { DatabaseType, NameTypePair } from '@/static/types';
import { CONTEXT_MENU_CONFIGS, REMOVED_MENU_ITEMS } from '@/static/types/DatabaseObjectTreeTypes';
import type { NodeData, DatabaseConfig, DatabaseInfo, MenuItemData } from '@/static/types/DatabaseObjectTreeTypes';
import { NodeType, MenuItemId } from '@/static/types/DatabaseObjectTreeTypes';
import { useCopy } from '@/components/Copy';
import useEmitter from '@/emitter';
import { NodeHelper } from './DatabaseObjectTreeHelper';
import { EVENT_OPEN_WEB_TERMINAL } from '@/static/ts/constants';
import { useDatabaseObjectStore } from '@/stores/DatabaseObjectStore';

/**
 * 对象树右键菜单的逻辑处理。
 *
 * @param tree 对象树 vue-tree ref。
 * @param contextMenu 右键菜单 v-contextmenu ref。
 * @returns 返回右键菜单需要使用的函数。
 */
export default function useDatabaseObjectTreeContextMenu({ tree, contextMenu }: { tree: any, contextMenu: any}) {
    // 菜单项，根据触发节点的上下文动态更新。
    const contextMenuItems = ref<MenuItemData[]>([]);

    // Event Bus.
    const emitter = useEmitter();

    // 注册菜单项和其对应的处理器。
    const menuItemHandlers: { [key: string ]: (item: MenuItemData) => void } = {
        [MenuItemId.COPY]: copyItem,
        [MenuItemId.UPDATE]: updateNode,
        [MenuItemId.WEB_TERMINAL]: openWebTerminal,
    }

    // 数据库对象 store。
    const dbStore = useDatabaseObjectStore();

    /**
     * 右键点击树节点时显示相应的右键菜单。
     *
     * @param config 节点所在分支的数据库的配置。
     * @param node 被点击的树节点。
     * @param event 鼠标事件。
     */
    function showContextMenu(config: DatabaseConfig, node: NodeData, event: MouseEvent): void {
        // 点击 document 任意地方，隐藏右键菜单和删除点击事件。
        function clickToHideContextMenu() {
            document.removeEventListener<'click'>('click', clickToHideContextMenu);
            contextMenu.value.hide();
        }

        // 禁止浏览器默认右键菜单。
        event.preventDefault();
        event.stopPropagation();

        // 计算得到需要显示的菜单项。
        // contextMenuItems.value =
        calculateMenuItems(config, node);

        if (contextMenuItems.value.length > 0) {
            contextMenu.value.show({ top: event.pageY, left: event.pageX });
            document.addEventListener<'click'>('click', clickToHideContextMenu);
        }
    }

    /**
     * 计算节点需要显示的菜单项。
     *
     * @param config 节点所在分支的数据库的配置。
     * @param node 被点击的树节点。
     * @returns 返回可用的菜单项。
     */
    function calculateMenuItems(config: DatabaseConfig, node: NodeData) {
        /*
         右键菜单项计算逻辑:
         1. 清空菜单项。
         2. 可见菜单项: 从数据库的所有菜单项中去掉被删除的菜单项。
         3. 可用菜单项: 被禁用的菜单项再根据当前状态决定是否启用 (可能需要发送请求从服务器查询使用条件，例如 DB Owner 才可用)。
         */

        // [1] 清空菜单项。
        contextMenuItems.value = [];

        // [2] 可见菜单项: 从数据库的所有菜单项中去掉被删除的菜单项。
        const itemConfigs = CONTEXT_MENU_CONFIGS[node.type] || [];              // 获取菜单项配置。
        const removedMenuItems = REMOVED_MENU_ITEMS[node.databaseType!] || []; // 被删除菜单项。

        for (const cfg of itemConfigs) {
            // 克隆创建菜单项。
            const item: MenuItemData = { ...cfg, node };

            // 去掉被删除的菜单项。
            if (removedMenuItems.includes(item.id)) {
                continue;
            }

            // 可见菜单项。
            contextMenuItems.value.push(item);
        }

        // [3] 可用菜单项: 被禁用的菜单项再根据当前状态决定是否启用 (可能需要发送请求从服务器查询使用条件，例如 DB Owner 才可用)。
        for (const item of contextMenuItems.value) {
            // 数据库实例没有连接时。
            if (NodeType.DATABASE_INSTANCE == item.node!.type && item.node!.connected) {
                if (MenuItemId.NEW_SQL_EDITOR === item.id) {
                    item.enabled = true;
                }
                if (MenuItemId.CLOSE_CONNECTION === item.id) {
                    item.enabled = true;
                }
            }

            // [注意] 模拟请求用户的权限，然后决定是否启用菜单项。
            if (MenuItemId.WEB_TERMINAL === item.id) {
                setTimeout(() => {
                    item.enabled = true;
                }, 500);
            }
        }
    }

    /**
     * 点击右键菜单项，根据菜单项 ID 执行相应的逻辑。
     *
     * @param item 菜单项对象。
     */
    function onContextMenuItemClicked(item: MenuItemData): void {
        // 查询菜单项的处理器，如果存在则执行。
        const handler = menuItemHandlers[item.id];
        if (handler) {
            handler(item);
        } else {
            console.error(`菜单项的处理器不存在: ID [${item.id}], Label [${item.label}]`);
        }
    }

    // 复制功能。
    const { copy } = useCopy();

    /**
     * 复制菜单项的内容，一般是复制触发它的树节点的 title。
     *
     * @param item 菜单项。
     */
    function copyItem(item: MenuItemData): void {
        copy(item.node!.title);
    }

    /**
     * 更新节点。
     *
     * @param item 菜单项。
     */
    function updateNode(item: MenuItemData): void {
        /*
         更新逻辑:
         1. 折叠 node。
         2. 删除 node 的所有孩子节点。
         3. 设置 node 的 _loaded 为 false，isLeaf 为 false。
         4. 从 store 中删除相关数据。
         4. 展开 node，自动重新加载节点的子节点。
         */

        const node: NodeData = item.node!;

        // [1] 折叠 node。
        tree.value.setExpand(node.id, false);

        // [2] 删除 node 的所有孩子节点。
        const childrenIds = node.children?.map(child => child.id) || [];
        for (const childId of childrenIds!) {
            tree.value.remove(childId);
        }

        // [3] 设置 node 的 _loaded 为 false，isLeaf 为 false。
        node._loaded = false;
        node.isLeaf = false;

        // [4] 从 store 中删除相关数据。
        removeRelatedDataFromStore(node);

        // [5] 展开 node，自动重新加载节点的子节点。
        tree.value.setExpand(node.id, true);
    }

    // 从 store 中删除相关数据。
    function removeRelatedDataFromStore(node: NodeData): void {
        // 节点的数据库类型。
        const dbType: DatabaseType = NodeHelper.findDatabaseType(node);

        // 删除实例的数据。
        if (node.type === NodeType.DATABASE_TYPE) {
            dbStore.removeDatabaseInstances(dbType);
            return;
        }

        // 删除实例下的数据。
        if (node.type === NodeType.DATABASE_INSTANCE) {
            dbStore.removeDataOfDatabaseInstance(dbType, node.dbid!);
            return;
        }

        // 删除 catalog, schema, table, procedure 等下的数据。
        const dbInfo: DatabaseInfo = NodeHelper.findDatabaseInfo(node);
        const pathElements: NameTypePair[] = [];

        // 构造节点的路径。
        if (dbInfo.catalog) {
            pathElements.push({ name: dbInfo.catalog, type: CATALOG });
        }
        if (dbInfo.schema) {
            pathElements.push({ name: dbInfo.schema, type: SCHEMA });
        }
        if (dbInfo.table) {
            pathElements.push({ name: dbInfo.table, type: TABLE });
        }
        if (dbInfo.view) {
            pathElements.push({ name: dbInfo.view, type: VIEW });
        }

        // 执行删除数据。
        dbStore.removeChildrenOfDatabaseObject(dbType, dbInfo.dbid, pathElements);
    }

    /**
     * 打开数据库终端。
     */
    function openWebTerminal(item: MenuItemData): void {
        const dbInfo: DatabaseInfo = NodeHelper.findDatabaseInfo(item.node!);
        emitter.emit(EVENT_OPEN_WEB_TERMINAL, dbInfo);
    }

    return { contextMenuItems, showContextMenu, onContextMenuItemClicked }
}

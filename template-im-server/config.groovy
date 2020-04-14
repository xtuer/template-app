// 运行: gradle clean run
// 打包: gradle clean shadowJar -Denv=production
// 部署: gradle clean deploy    -Denv=production

////////////////////////////////////////////////////////////////////////////////////
//                               定义所有环境下都有的通用配置
////////////////////////////////////////////////////////////////////////////////////
//environments {
//  usedByAllEnvironments {
        deploy {
            host     = '127.0.0.1'
            username = 'root'
            password = 'root'
            dir      = '/www.training.com' // 部署目录
        }

        mongodb {
            host = 'mongo.training'
            port = 27017
            database = 'training'
            username = 'training'
            password = 'training'
        }

        logDirectory  = '/training/logs/im' // 日志目录
//  }
//}

////////////////////////////////////////////////////////////////////////////////////
//                定义不同环境下特有的配置，同路径时覆盖上面定义的通用配置
////////////////////////////////////////////////////////////////////////////////////
environments {
    // Mac 开发环境配置
    mac {
        mongodb {
            database = 'ebag'
            username = 'ebag'
            password = 'ebag'
        }

        logDirectory  = '/usr/local/temp/xtuer/logs/im' // 日志目录
    }

    // 测试环境
    test1 {
        deploy {
            host     = '47.99.136.29'
            username = 'root'
            password = 'temp_hxdd'
        }
    }
}

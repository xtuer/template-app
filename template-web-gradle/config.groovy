// 打包: gradle clean build  -Denv=production
// 部署: gradle clean deploy -Denv=production

////////////////////////////////////////////////////////////////////////////////////
//                               定义所有环境下都有的通用配置
////////////////////////////////////////////////////////////////////////////////////
//environments {
//  usedByAllEnvironments {
        deploy {
            hostname = '127.0.0.1'
            username = 'root'
            password = 'root'
        }

        database {
            url = 'jdbc:mysql://localhost:3306/ebag'
            username = 'root'
            password = 'root'
        }

        redis {
            host = '127.0.0.1'
            port = 6379
            password = ''
            database = 0
            timeout  = 2000
        }

        // 应用的 ID 和 key，用于生成身份认证的 token
        appId  = 'Default_ID'
        appKey = 'Default_Key'

        idWorker = 0              // ID 生成器的 ID，范围是 [0, 1023]
        thymeleafCacheable = true // thymeleaf 默认使用缓存提高效率
        tempDir = '/temp'         // 临时文件目录，例如存储上传的临时文件，里面的文件可以超过几天不放问可以用 crontab 自动删除
//  }
//}

////////////////////////////////////////////////////////////////////////////////////
//                定义不同环境下特有的配置，同路径时覆盖上面定义的通用配置
////////////////////////////////////////////////////////////////////////////////////
environments {
    // 开发环境配置
    development {
        database {
            url = 'jdbc:mysql://office.edu-edu.com:3306/ebag'
        }
        redis {
            host = 'office.edu-edu.com'
        }
        thymeleafCacheable = false // 开发环境下 thymeleaf 不缓存页面
    }

    localhost {
        thymeleafCacheable = false // 开发环境下 thymeleaf 不缓存页面
    }

    // 线上环境配置
    production {
        // 线上环境不同机器的部署信息不一样
        deploy {
            hostname = '120.92.26.194'
            username = 'root'
            password = 'xxxx'
        }

        // 线上环境的数据库信息不一样
        database {
            url = 'jdbc:mysql://localhost:3306/ebag'
            username = 'root'
            password = 'huaxia-123'
        }

        // 线上环境不同机器的 idWorker 必须不一样
        idWorker = 1
    }
}

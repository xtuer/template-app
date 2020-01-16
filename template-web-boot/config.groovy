// 部署: gradle clean deploy -Denv=test1

////////////////////////////////////////////////////////////////////////////////////
//                               定义所有环境下都有的通用配置
////////////////////////////////////////////////////////////////////////////////////
//environments {
//  usedByAllEnvironments {
        deploy {
            dir  = '/www.xtuer.com' // 部署目录
            port = 8080             // 程序端口
            username = 'root'       // 服务器账号
            password = 'root'       // 服务器密码
        }
//  }
//}

environments {
    test1 {
        deploy {
            port     = 8888
            host     = '192.168.10.173'
            username = 'root'
            password = 'tiger_sun'
        }
    }
}

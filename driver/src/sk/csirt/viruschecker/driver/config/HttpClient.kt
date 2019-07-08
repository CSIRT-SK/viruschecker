//package driver.sk.csirt.config
//
//import io.ktor.client.HttpClient
//import io.ktor.client.engine.apache.Apache
//import org.apache.http.ssl.SSLContextBuilder
//
//val httpClient = HttpClient(Apache){
//    engine {
//        customizeClient {
//            setSSLContext(
//                SSLContextBuilder
//                    .create()
////                    .loadTrustMaterial(TrustSelfSignedStrategy())
//                    .build()
//            )
////            setSSLHostnameVerifier(NoopHostnameVerifier())
//        }
//    }
//}
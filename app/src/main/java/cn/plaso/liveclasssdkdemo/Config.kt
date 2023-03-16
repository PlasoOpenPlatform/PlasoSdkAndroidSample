package cn.plaso.liveclasssdkdemo

object Config {
    val ossToken: String = "{\n" +
            "      \"expire\": 1603268701,\n" +
            "      \"accessKeyId\": \"STS.NUr2aqH5BUfbQVqHckrn618Hr\",\n" +
            "      \"accessKeySecret\": \"C8eEou779NFgwRqhmPBX1dy2Nz7mVWPFxjeJjVuXab9J\",\n" +
            "      \"stsToken\": \"CAISjQN1q6Ft5B2yfSjIr5bHedvFpepj4qSJU3DArGM+fuEa3v3jkDz2IHFIfXNtAOAftPo+mGpT5vwZlqAqFscdHRGfWpMoRia0S7/nMeT7oMWQweEuPfTHcDHhtHeZsebWZ+LmNuG/Ht6md1HDkAJq3LL+bk/Mdle5MJqP+7QFFtMMRVuAcCZhDtVbLRdLo8t4UHzKLqSVLwLNiGjdB1YK3w1nkjFQ5LiYyM+R4Qa89Frh0b06qpjWKJqpZNVFN5MFSbXT5uFtcbfb2yN98gVD8LwM7JZJ4jDapNqQcWcz3xyNKLjT6cY9bl07NKY7H6MBtPz1jvo/tuHN0pzzwg1KeuRUVSWYHdv6nYydRrvybIhmJe6iZy6Vl4jJasar7Vl1MS5BZV8QKoR8eiM2ERglUTadaI3foQCWP1v+FvnbjPlnjcAokW+Fp4TaewK9JJyCyjsdN5MGaEclCgUbx2SJcNVdIlAVKQ4+V+/PE9QvNksO8/jyxArJTWh6w3hbuPv6drbMvaQSeV1p9mPFdEF2GoABfw1SWlMPoHyfJzRminVJDpN0Gc0ibNtKNFR5cdT0msmvM37dMJo8s/cDp2EU2pIa225P5685NM/16vxnCeVmA4k2OP9zn23wBRvevspycoj32oV2qpsba2ns3PQ8oCxzHTjRtyO0X/tni/Pz0Ir0mM7uu5rAOHzqUIUH28r5/RM=\",\n" +
            "      \"uploadPath\": \"dev-plaso/mini/1325/1603267801420-0.47662984294296584.plaso\",\n" +
            "      \"provider\": \"OSS\",\n" +
            "      \"bucket\": \"file-plaso\",\n" +
            "      \"protocol\": \"https\",\n" +
            "      \"region\": \"oss-cn-hangzhou\",\n" +
            "      \"domain\": \"aliyuncs.com\"\n" +
            "    }"
    var server = "https://dev.plaso.cn"

    const val oldServerPrefix = "https://dev.plaso.cn/static/yxtsdk/?"

    /**
     * the appId get from plaso
     */
    var appId: String = ""


    /**
     * appKey
     */
    var appKey:String = ""
}
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


val Log: Logger = LoggerFactory.getLogger("boss")
var isResume = AtomicBoolean(false)
var sendNum = 0

/**
 * SeaDumping 海投主程序 适用于boss
 * var startBoss = 1 将开启投递
 * var startBoss = 0 将关闭投递
 */
fun main() {
    val options = ChromeOptions()
    options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
    val latch = CountDownLatch(1) // 创建一个计数器，初始值为1
    Log.info("开始打开浏览器")
    val chromeDriver = ChromeDriver(options)
    // 打开boss直聘
    chromeDriver.get("https://login.zhipin.com/")
    // 同意合约
    chromeDriver.findElement(By.className("agree-policy")).click()
    // 打开WX 扫一扫二维码
    chromeDriver.findElement(By.className("wx-login-btn")).click()

    val work = {
        Log.info("开始检测。。。。。")
        // 检测是否进去找工作页面
        try {
            while (true) {
                waitForRefresh(chromeDriver)
                if (isResume.get()) {
                    Log.info("开始投递简历")
                    startSend(chromeDriver)
                }
            }
        } catch (i: InterruptedException) {
            Thread.currentThread().interrupt()
            Log.info("收到中断将要重新启动")
        }
    }

    val workThread = Thread(work, "工作线程work")

    Thread({
        while (true) {
            startBoss(chromeDriver)
            delayDriver(4000)
        }
    }, "检测线程").start()

    Thread({
        // 模拟随机鼠标移动
        val actions = Actions(chromeDriver)
        val random = Random()

        // 模拟随机的鼠标移动

        // 模拟随机的鼠标移动
        while (true) {
            // 等待一段时间，模拟人类用户的停留行为
            try {
                delayDriver(random.nextInt(2000) + 1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if(!isResume.get()){
                delayDriver(random.nextInt(2000) + 1000L)
                continue
            }
            val xOffset: Int = random.nextInt(200) - 100 // 随机横向移动
            val yOffset: Int = random.nextInt(200) - 100 // 随机纵向移动
            actions.moveToElement(chromeDriver.findElement(By.tagName("body")), xOffset, yOffset).perform()


        }
    }, "鼠标模拟线程").start()

    workThread.start()
    latch.await()
}


/**
 * 遍历每一个招聘信息开始投递
 */
fun forSend(driver: WebDriver) {
    while (isResume.get()) {
        val webElements = driver.findElement(By.className("job-list-box")).findElements(By.className("info-public"))
        if (sendNum >= webElements.size) {
            sendNum = 0
            return
        }
        val send = webElements[sendNum]
        val url = driver.currentUrl
        // 进行投递
        send.click()
        delayDriver(2000)
        /*
         防止未能跳转到上一页
         这段代码若不暂停会页面不会跳回
         */
        while (driver.currentUrl != url && isResume.get()) {
            driver.navigate().back()
            delayDriver(1000)
            Log.info("返回上一页")
        }
        Log.info("进行下一个投递")
        delayDriver(2000)
        sendNum++
    }
}


/**
 * 开始打招呼🙋 start
 */
fun startSend(driver: WebDriver) {
    try {
        forSend(driver)
        Log.info("进入下一页")
        if (!isResume.get()) {
            return
        }
        // 判断是否还有下一页
        val aList = driver.findElement(By.className("options-pages")).findElements(By.tagName("a"))
        val disabled = aList.last().getAttribute("class")
        if ("disabled" == disabled) {
            isResume.set(false)
            Log.info("投递结束。。。。")
        } else {
            // 进入下一页
            aList.last().click()
        }
    } catch (e: Exception) {
        Log.error(e.toString())
    }
}

fun delayDriver(time: Long) {
    TimeUnit.MILLISECONDS.sleep(time)
}

/**
 * 用于页面等待刷新完成 ps：感觉没什么用
 */
fun waitForRefresh(driver: WebDriver) {
    val jsExecutor = driver as JavascriptExecutor
    val script = "return document.readyState"
    var pageLoadStatus = jsExecutor.executeScript(script) as String
    // 等待页面加载完成
    while (pageLoadStatus != "complete") {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        pageLoadStatus = jsExecutor.executeScript(script) as String
    }
}

/**
 * 用于判断用户开启投递
 */
fun startBoss(driver: WebDriver): Boolean {
    val jsExecutor = driver as JavascriptExecutor
    val variableName = "startBoss" // 替换成你要获取的变量名
    val script = "return $variableName;"
    try {
        val variableValue = jsExecutor.executeScript(script)
        // variableValue 为null 但是 isResume 为true继续执行
        if (variableValue != null) {
            isResume.set("1" == variableValue.toString())
        }
    } catch (_: Exception) {
        // 未有变量该代码报错。。。

    }
    if (!isResume.get()) {
        Log.info("等待投递中。。。。。")
    }

    return isResume.get()
}
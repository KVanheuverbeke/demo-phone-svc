package be.demo.normalizephone;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@SpringBootTest
class NormalizePhoneApplicationSpec extends Specification {

    @Autowired
    ApplicationContext context

    def 'should start successfully'() {
        expect:
        context
    }

}
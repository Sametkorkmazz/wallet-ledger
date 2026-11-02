package com.sametkorkmaz.walletledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WalletledgerApplicationTests {

	@Test
	void contextLoads() {
	}

}

package com.tableorder.repository;

import com.tableorder.entity.Category;
import com.tableorder.entity.Menu;
import com.tableorder.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MenuRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MenuRepository menuRepository;

    private Long storeId;
    private Long otherStoreId;
    private Long categoryId;
    private Long otherCategoryId;

    @BeforeEach
    void setUp() {
        Store store = Store.builder().storeCode("STORE001").storeName("테스트 매장").build();
        entityManager.persist(store);

        Store otherStore = Store.builder().storeCode("STORE002").storeName("다른 매장").build();
        entityManager.persist(otherStore);

        Category category = Category.builder().storeId(store.getId()).name("메인").displayOrder(0).build();
        entityManager.persist(category);

        Category otherCategory = Category.builder().storeId(store.getId()).name("음료").displayOrder(1).build();
        entityManager.persist(otherCategory);

        entityManager.flush();
        storeId = store.getId();
        otherStoreId = otherStore.getId();
        categoryId = category.getId();
        otherCategoryId = otherCategory.getId();
    }

    @Test
    @DisplayName("매장별 전체 메뉴를 displayOrder 순으로 조회한다")
    void findByStoreIdOrderByDisplayOrder() {
        Menu menu1 = Menu.builder().storeId(storeId).categoryId(categoryId)
                .name("김치찌개").price(8000).displayOrder(2).build();
        Menu menu2 = Menu.builder().storeId(storeId).categoryId(categoryId)
                .name("된장찌개").price(7000).displayOrder(1).build();
        Menu menu3 = Menu.builder().storeId(otherStoreId).categoryId(categoryId)
                .name("비빔밥").price(9000).displayOrder(0).build();

        entityManager.persist(menu1);
        entityManager.persist(menu2);
        entityManager.persist(menu3);
        entityManager.flush();

        List<Menu> result = menuRepository.findByStoreIdOrderByDisplayOrder(storeId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("된장찌개");
        assertThat(result.get(1).getName()).isEqualTo("김치찌개");
    }

    @Test
    @DisplayName("카테고리별 메뉴를 displayOrder 순으로 조회한다")
    void findByStoreIdAndCategoryIdOrderByDisplayOrder() {
        Menu menu1 = Menu.builder().storeId(storeId).categoryId(categoryId)
                .name("김치찌개").price(8000).displayOrder(1).build();
        Menu menu2 = Menu.builder().storeId(storeId).categoryId(otherCategoryId)
                .name("콜라").price(2000).displayOrder(0).build();

        entityManager.persist(menu1);
        entityManager.persist(menu2);
        entityManager.flush();

        List<Menu> result = menuRepository.findByStoreIdAndCategoryIdOrderByDisplayOrder(storeId, categoryId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("김치찌개");
    }

    @Test
    @DisplayName("매장 격리된 메뉴 단건 조회 - 성공")
    void findByIdAndStoreId_success() {
        Menu menu = Menu.builder().storeId(storeId).categoryId(categoryId)
                .name("김치찌개").price(8000).displayOrder(0).build();
        entityManager.persist(menu);
        entityManager.flush();

        Optional<Menu> result = menuRepository.findByIdAndStoreId(menu.getId(), storeId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("김치찌개");
    }

    @Test
    @DisplayName("매장 격리된 메뉴 단건 조회 - 다른 매장이면 빈 결과")
    void findByIdAndStoreId_otherStore() {
        Menu menu = Menu.builder().storeId(storeId).categoryId(categoryId)
                .name("김치찌개").price(8000).displayOrder(0).build();
        entityManager.persist(menu);
        entityManager.flush();

        Optional<Menu> result = menuRepository.findByIdAndStoreId(menu.getId(), otherStoreId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("카테고리에 메뉴 존재 여부 확인 - 존재하는 경우")
    void existsByCategoryId_exists() {
        Menu menu = Menu.builder().storeId(storeId).categoryId(categoryId)
                .name("김치찌개").price(8000).displayOrder(0).build();
        entityManager.persist(menu);
        entityManager.flush();

        assertThat(menuRepository.existsByCategoryId(categoryId)).isTrue();
    }

    @Test
    @DisplayName("카테고리에 메뉴 존재 여부 확인 - 존재하지 않는 경우")
    void existsByCategoryId_notExists() {
        assertThat(menuRepository.existsByCategoryId(999L)).isFalse();
    }
}

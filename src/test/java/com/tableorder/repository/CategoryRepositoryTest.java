package com.tableorder.repository;

import com.tableorder.entity.Category;
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
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long storeId;
    private Long otherStoreId;

    @BeforeEach
    void setUp() {
        Store store = Store.builder()
                .storeCode("STORE001")
                .storeName("테스트 매장")
                .build();
        entityManager.persist(store);

        Store otherStore = Store.builder()
                .storeCode("STORE002")
                .storeName("다른 매장")
                .build();
        entityManager.persist(otherStore);

        entityManager.flush();
        storeId = store.getId();
        otherStoreId = otherStore.getId();
    }

    @Test
    @DisplayName("매장별 카테고리를 displayOrder 순으로 조회한다")
    void findByStoreIdOrderByDisplayOrder() {
        Category cat1 = Category.builder().storeId(storeId).name("음료").displayOrder(2).build();
        Category cat2 = Category.builder().storeId(storeId).name("메인").displayOrder(1).build();
        Category cat3 = Category.builder().storeId(otherStoreId).name("사이드").displayOrder(0).build();

        entityManager.persist(cat1);
        entityManager.persist(cat2);
        entityManager.persist(cat3);
        entityManager.flush();

        List<Category> result = categoryRepository.findByStoreIdOrderByDisplayOrder(storeId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("메인");
        assertThat(result.get(1).getName()).isEqualTo("음료");
    }

    @Test
    @DisplayName("매장 격리된 카테고리 단건 조회 - 성공")
    void findByIdAndStoreId_success() {
        Category category = Category.builder().storeId(storeId).name("메인").displayOrder(0).build();
        entityManager.persist(category);
        entityManager.flush();

        Optional<Category> result = categoryRepository.findByIdAndStoreId(category.getId(), storeId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("메인");
    }

    @Test
    @DisplayName("매장 격리된 카테고리 단건 조회 - 다른 매장이면 빈 결과")
    void findByIdAndStoreId_otherStore() {
        Category category = Category.builder().storeId(storeId).name("메인").displayOrder(0).build();
        entityManager.persist(category);
        entityManager.flush();

        Optional<Category> result = categoryRepository.findByIdAndStoreId(category.getId(), otherStoreId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("카테고리명 중복 검사 - 존재하는 경우")
    void existsByStoreIdAndName_exists() {
        Category category = Category.builder().storeId(storeId).name("메인").displayOrder(0).build();
        entityManager.persist(category);
        entityManager.flush();

        assertThat(categoryRepository.existsByStoreIdAndName(storeId, "메인")).isTrue();
    }

    @Test
    @DisplayName("카테고리명 중복 검사 - 다른 매장은 중복 아님")
    void existsByStoreIdAndName_otherStore() {
        Category category = Category.builder().storeId(storeId).name("메인").displayOrder(0).build();
        entityManager.persist(category);
        entityManager.flush();

        assertThat(categoryRepository.existsByStoreIdAndName(otherStoreId, "메인")).isFalse();
    }

    @Test
    @DisplayName("카테고리명 중복 검사 (자기 제외) - 수정 시 사용")
    void existsByStoreIdAndNameAndIdNot() {
        Category cat1 = Category.builder().storeId(storeId).name("메인").displayOrder(0).build();
        Category cat2 = Category.builder().storeId(storeId).name("음료").displayOrder(1).build();
        entityManager.persist(cat1);
        entityManager.persist(cat2);
        entityManager.flush();

        // cat2의 이름을 "메인"으로 변경하려 할 때 → 중복
        assertThat(categoryRepository.existsByStoreIdAndNameAndIdNot(storeId, "메인", cat2.getId())).isTrue();
        // cat1의 이름을 "메인"으로 유지할 때 → 자기 자신이므로 중복 아님
        assertThat(categoryRepository.existsByStoreIdAndNameAndIdNot(storeId, "메인", cat1.getId())).isFalse();
    }
}

package dev.makeev.search.repository;

import dev.makeev.search.model.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class ElasticsearchSearchRepositoryImpl {

    private final ReactiveElasticsearchOperations elasticsearchOperations;

    public Flux<ProductDocument> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name) {
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("name").contains(name)
        );
        query.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));
        return elasticsearchOperations.search(query, ProductDocument.class)
                .map(SearchHit::getContent);
    }

    public Flux<ProductDocument> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category) {
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("category").is(category)
        );
        query.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));
        return elasticsearchOperations.search(query, ProductDocument.class)
                .map(SearchHit::getContent);
    }

    public Flux<ProductDocument> findByTagsContainingOrderByCreatedAtDesc(String tag) {
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("tags").contains(tag)
        );
        query.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));
        return elasticsearchOperations.search(query, ProductDocument.class)
                .map(SearchHit::getContent);
    }

    public Flux<ProductDocument> findByPriceBetweenOrderByPriceAsc(BigDecimal minPrice, BigDecimal maxPrice) {
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("price").between(minPrice, maxPrice)
        );
        query.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.ASC, "price"
        ));
        return elasticsearchOperations.search(query, ProductDocument.class)
                .map(SearchHit::getContent);
    }

    public Flux<ProductDocument> searchByText(String query) {
        if (query == null || query.trim().isEmpty()) {
            return elasticsearchOperations.search(new CriteriaQuery(new Criteria()), ProductDocument.class).map(SearchHit::getContent);
        }

        CriteriaQuery searchQuery = new CriteriaQuery(
            new Criteria("name").contains(query)
                .or("description").contains(query)
                .or("tags").contains(query)
        );
        searchQuery.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));
        return elasticsearchOperations.search(searchQuery, ProductDocument.class)
                .map(SearchHit::getContent);
    }

    public Flux<ProductDocument> searchWithFilters(String query, String category, String tags, BigDecimal minPrice, BigDecimal maxPrice) {
        Criteria criteria = new Criteria();

        if (query != null && !query.trim().isEmpty()) {
            criteria = criteria.and(
                new Criteria("name").contains(query)
                    .or("description").contains(query)
            );
        }

        if (category != null && !category.trim().isEmpty()) {
            criteria = criteria.and("category").is(category);
        }

        if (tags != null && !tags.trim().isEmpty()) {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    criteria = criteria.and("tags").contains(trimmedTag);
                }
            }
        }

        if (minPrice != null && maxPrice != null) {
            criteria = criteria.and("price").between(minPrice, maxPrice);
        }

        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        searchQuery.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));

        return elasticsearchOperations.search(searchQuery, ProductDocument.class)
                .map(SearchHit::getContent);
    }

    public Mono<Long> countByCategoryIgnoreCase(String category) {
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("category").is(category)
        );
        return elasticsearchOperations.count(query, ProductDocument.class);
    }

    public Flux<ProductDocument> findTop10ByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name) {
        return findByNameContainingIgnoreCaseOrderByCreatedAtDesc(name).take(10);
    }

    public Flux<ProductDocument> findTop10ByOrderByCreatedAtDesc() {
        CriteriaQuery query = new CriteriaQuery(new Criteria());
        query.addSort(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
        ));
        query.setMaxResults(10);
        return elasticsearchOperations.search(query, ProductDocument.class)
                .map(SearchHit::getContent);
    }
}

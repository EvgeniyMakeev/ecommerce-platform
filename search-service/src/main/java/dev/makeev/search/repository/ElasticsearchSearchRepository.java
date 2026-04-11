package dev.makeev.search.repository;

import dev.makeev.search.model.ProductDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchSearchRepository extends ReactiveElasticsearchRepository<ProductDocument, String> {
}

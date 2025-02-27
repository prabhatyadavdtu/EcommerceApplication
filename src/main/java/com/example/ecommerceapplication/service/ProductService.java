package com.example.ecommerceapplication.service;

import com.example.ecommerceapplication.exception.ResourceNotFoundException;
import com.example.ecommerceapplication.model.Product;
import com.example.ecommerceapplication.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getProduct(id);
        BeanUtils.copyProperties(product, existingProduct, "id");
        return productRepository.save(existingProduct);
    }
}

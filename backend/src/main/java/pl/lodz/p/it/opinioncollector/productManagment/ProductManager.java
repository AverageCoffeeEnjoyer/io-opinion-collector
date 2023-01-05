package pl.lodz.p.it.opinioncollector.productManagment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.opinioncollector.category.managers.CategoryManager;
import pl.lodz.p.it.opinioncollector.category.model.Category;
import pl.lodz.p.it.opinioncollector.eventHandling.IProductEventManager;
import pl.lodz.p.it.opinioncollector.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.opinioncollector.exceptions.products.ProductNotFoundException;
import pl.lodz.p.it.opinioncollector.userModule.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductManager implements IProductManager {
    private final ProductRepository productRepository;
    private final IProductEventManager eventManager;
    private final CategoryManager categoryManager;

    @Autowired
    public ProductManager(ProductRepository productRepository, IProductEventManager eventManager,
                          CategoryManager categoryManager) {
        this.productRepository = productRepository;
        this.eventManager = eventManager;
        this.categoryManager = categoryManager;
    }

    // brak createProduct, ponieważ nie jest on potrzebny

    public Product getProduct(UUID constantProductId) {
        List<Product> product = productRepository
                .findByProductIdAndDeletedFalseAndConfirmedTrue(constantProductId);
        if(product.isEmpty()) {
            return null;
        }
        return product.get(0);
    }

    // updateProduct jest tak naprawdę stworzeniem od razu potwierdzonej sugestii
    // categoryId nie jest brane pod uwagę przy updacie
    public Product updateProduct(UUID productId, ProductDTO productDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Product product = getProduct(productId);
        updateProduct(product, productDTO);
        product.setConfirmed(true);

        productRepository.save(product);
        eventManager.createProductReportEvent(user.getId(), "New product suggestion with name: \""
                        + product.getName() + "\" and description: \"" + product.getDescription() + "\"",
                product.getUniqueProductId());
        return product;
    }

    // createSuggestion zostało rozbite na 3 metody:
    //     - createCreateSuggestion - tworzenie nowych produktów
    //     - createUpdateSuggestion - modyfikacja istniejącego produktu
    //     - createDeleteSuggestion - usuwanie produktu

    public Product createCreateSuggestion(ProductDTO productDTO) {
        Category category;
        try {
            category = categoryManager.getCategory(productDTO.getCategoryId());
        } catch (CategoryNotFoundException e) {
            return null;
        }

        Product product = new Product(productDTO, category);
        productRepository.save(product);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        eventManager.createProductReportEvent(user.getId(), "New product suggestion with name: \""
                        + product.getName() + "\" and description: \"" + product.getDescription() + "\"",
                product.getUniqueProductId());
        return product;
    }

    public Product createUpdateSuggestion(UUID productId, ProductDTO productDTO) throws ProductNotFoundException {
        Product product = getProduct(productId);
        if(product == null) {
            throw new ProductNotFoundException();
        }

        updateProduct(product, productDTO);

        productRepository.save(product);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        eventManager.createProductReportEvent(user.getId(), "New product suggestion with name: \""
                        + product.getName() + "\" and description: \"" + product.getDescription() + "\"",
                product.getUniqueProductId());
        return product;
    }

    public boolean createDeleteSuggestion(UUID productId, ProductDeleteForm productDF) {
        Product product = getProduct(productId);
        if(product == null) {
            return false;
        }

        updateProduct(product);

        productRepository.save(product);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        eventManager.createProductReportEvent(user.getId(), "User requested deletion of" +
                " a product with description: \"" + productDF.getDescription() + "\"", productId);
        return true;
    }

    // to jest identyfikowane poprzez uniqueProductId
    public boolean confirmProduct(UUID suggestionId) {
        Optional<Product> productOptional = productRepository.findById(suggestionId);
        if (productOptional.isPresent()) {
            productOptional.get().setConfirmed(true);
            productRepository.save(productOptional.get());
            return true;
        }
        return false;
    }

    // to jest identyfikowane poprzez uniqueProductId
    public boolean unconfirmProduct(UUID uuid) {
        Optional<Product> productOptional = productRepository.findById(uuid);
        if (productOptional.isPresent()) {
            productOptional.get().setConfirmed(false);
            productRepository.save(productOptional.get());
            return true;
        }
        return false;
    }



    public boolean deleteProduct(UUID uuid) {
        Optional<Product> product = productRepository.findById(uuid);
        if (product.isPresent()) {
            product.get().setDeleted(true);
            productRepository.save(product.get());
            return true;
        }
        return false;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(UUID uuid) {
        return productRepository.findByCategoryCategoryID(uuid);
    }

    public List<Product> getUnconfirmedSuggestions() {
        return productRepository.findByConfirmedFalse();
    }

    private Product updateProduct(Product product, ProductDTO productDTO) {
        updateProduct(product);
        product.setName(productDTO.getName());
        product.setDescription(product.getDescription());
        product.setProperties(productDTO.getProperties());
        product.setConfirmed(false);
        return product;
    }

    private Product updateProduct(Product product) {
        product.setUniqueProductId(UUID.randomUUID());
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }


}

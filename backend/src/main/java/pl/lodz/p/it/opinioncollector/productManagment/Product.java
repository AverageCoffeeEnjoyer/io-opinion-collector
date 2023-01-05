package pl.lodz.p.it.opinioncollector.productManagment;


import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.opinioncollector.category.model.Category;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Valid
@NoArgsConstructor
public class Product implements Serializable {

    @Id
    @Column(nullable = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uniqueProductId;

    @Column(name = "product_id")
    private UUID productId;

    @ManyToOne(optional = false) //cascade?
    @JoinColumn(name = "category_id", referencedColumnName = "categoryid", nullable = false)
    private Category category;

    private String name;

    private String description;

    private boolean deleted;

    private boolean confirmed;

    @ElementCollection
    @CollectionTable(name = "properties")
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    @JoinColumn(name = "product_id")
    private Map<String, String> properties = new HashMap<>();

    LocalDateTime createdAt;

    public Product(UUID categoryId, String name, String description) {
        this.name = name;
        this.description = description;
        this.deleted = false;
        this.confirmed = true;
    }


    // creates "add product" suggestion
    public Product(ProductDTO productDTO, Category category) {
        this.uniqueProductId = UUID.randomUUID();
        this.productId = UUID.randomUUID();
        this.name = productDTO.getName();
        this.category = category;
        this.description = productDTO.getDescription();
        this.deleted = false;
        this.confirmed = false;
        this.properties = productDTO.getProperties();
        this.createdAt = LocalDateTime.now();
    }


    public void removeProperty(String key) {
        properties.remove(key);
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperties(Map<String, String> properties) {
        for (String key :
                properties.keySet()) {
            this.properties.put(key, properties.get(key)); //FIXME but now key has to be unique, hashset?
        }
//            this.properties.putIfAbsent(key, properties.get(key)); // How about that?
//            if(this.properties.putIfAbsent(key, properties.get(key)) == null){
//            this.properties.put(key + "idk_something to distinct", properties.get(key));
//            }  // And this?
    }
}

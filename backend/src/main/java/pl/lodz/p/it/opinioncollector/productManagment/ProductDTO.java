package pl.lodz.p.it.opinioncollector.productManagment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.UUID;

@Data
@Valid
@NoArgsConstructor
public class ProductDTO {

    @NotNull
    private UUID categoryId;

    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    @NotEmpty
    HashMap<String, String> properties;


    public ProductDTO(UUID categoryId, String name, String description, HashMap<String,
            String> properties) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.properties = properties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("categoryId=").append(categoryId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", properties='").append(properties).append('\'');
        return sb.toString();
    }
}

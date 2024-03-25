package dat3.recipe.api;

import dat3.recipe.dto.RecipeDto;
import dat3.recipe.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<RecipeDto> getAllRecipes(@RequestParam(required = false) String category) {
        return recipeService.getAllRecipes(category);
    }
    @GetMapping(path ="/{id}")
    public RecipeDto getRecipeById(@PathVariable int id) {
        return recipeService.getRecipeById(id);
    }

    @PreAuthorize("hasRole('USER' || 'ADMIN')")
    @PostMapping
    public RecipeDto addRecipe(@RequestBody RecipeDto request) {
        return recipeService.addRecipe(request);
    }
    @PreAuthorize("hasRole('USER'|| 'ADMIN')")
    @PutMapping(path = "/{id}")
    public RecipeDto editRecipe(@RequestBody RecipeDto request,@PathVariable int id) {
        boolean isAdmin = isAdmin(SecurityContextHolder.getContext().getAuthentication());
        String userName = getUsername(SecurityContextHolder.getContext().getAuthentication());
        RecipeDto recipe = recipeService.getRecipeById(id);

        if (isAdmin || userName.equals(recipe.getOwner())) {
            return recipeService.editRecipe(request, id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this recipe");
        }
    }
    @PreAuthorize("hasRole('USER'|| 'ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<RecipeDto> deleteRecipe(@PathVariable int id, Authentication auth) {
        boolean isAdmin = isAdmin(SecurityContextHolder.getContext().getAuthentication());
        String userName = getUsername(SecurityContextHolder.getContext().getAuthentication());
        RecipeDto recipe = recipeService.getRecipeById(id);

        if (isAdmin || userName.equals(recipe.getOwner())) {
            recipeService.deleteRecipe(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this recipe");
        }
    }

    private boolean isAdmin(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return roles.contains("ADMIN");
    }

    private String getUsername(Authentication auth) {
        return auth.getName();
    }
}
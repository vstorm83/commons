package org.exoplatform.commons.search.driver.jcr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.search.Search;
import org.exoplatform.commons.search.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class JcrPeopleSearch implements Search {
  private static final String SEARCH_TYPE_NAME = "people";
  
  public Collection<SearchResult> search(String query) {
    Collection<SearchResult> searchResults = new ArrayList<SearchResult>();
    Collection<JcrSearchResult> jcrResults = JcrSearchService.search("type=" + SEARCH_TYPE_NAME + " repository=repository workspace=social from=soc:profiledefinition " + query);
    IdentityManager identityManager = (IdentityManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);

    for(JcrSearchResult jcrResult: jcrResults) {
      try {
        @SuppressWarnings("unchecked")
        String username = ((List<String>)jcrResult.getProperty("void-username")).get(0);
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
        Profile profile = identity.getProfile();

        SearchResult result = new SearchResult("people", profile.getUrl());
        result.setTitle(profile.getFullName());
        String position = profile.getPosition();
        if(null == position) position = "";
        result.setExcerpt(position);
        result.setDetail(profile.getEmail());
        String avatar = profile.getAvatarUrl();      
        if(null == avatar) avatar = "/social-resources/skin/ShareImages/Avatar.gif";
        result.setAvatar(avatar);

        searchResults.add(result);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }

    return searchResults;
  }

}
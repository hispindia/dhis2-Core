package org.hisp.dhis.ivb.favorite.action;

import org.hisp.dhis.favorite.Favorite;
import org.hisp.dhis.favorite.FavoriteService;

import com.opensymphony.xwork2.Action;

public class DeleteFavoriteAction implements Action {

	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------
	private FavoriteService favoriteService;

	public void setFavoriteService(FavoriteService favoriteService) {
		this.favoriteService = favoriteService;
	}
	
	// -------------------------------------------------------------------------
	// Input / Output
	// -------------------------------------------------------------------------

	 private String favoriteName;
	    
	public void setFavoriteName(String favoriteName) {
		this.favoriteName = favoriteName;
	}



	@Override
	public String execute() throws Exception {
		
		System.out.println("Favorite name : "+favoriteName);
		
		try {
			Favorite favorite = favoriteService.getFavoriteByName(favoriteName);
			favoriteService.deleteFavorite(favorite);
			
		//	favoriteService.deleteFavoriteCustom(favorite.getId());
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
			
		return SUCCESS;
	}

}

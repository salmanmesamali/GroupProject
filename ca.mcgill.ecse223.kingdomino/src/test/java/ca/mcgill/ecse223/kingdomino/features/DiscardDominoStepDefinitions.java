package ca.mcgill.ecse223.kingdomino.features;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ca.mcgill.ecse223.kingdomino.KingdominoApplication;
import ca.mcgill.ecse223.kingdomino.controller.KingdominoController;
import ca.mcgill.ecse223.kingdomino.model.Castle;
import ca.mcgill.ecse223.kingdomino.model.Domino;
import ca.mcgill.ecse223.kingdomino.model.Domino.DominoStatus;
import ca.mcgill.ecse223.kingdomino.model.DominoInKingdom;
import ca.mcgill.ecse223.kingdomino.model.DominoInKingdom.DirectionKind;
import ca.mcgill.ecse223.kingdomino.model.DominoSelection;
import ca.mcgill.ecse223.kingdomino.model.Draft.DraftStatus;
import ca.mcgill.ecse223.kingdomino.model.Draft;
import ca.mcgill.ecse223.kingdomino.model.Game;
import ca.mcgill.ecse223.kingdomino.model.Kingdom;
import ca.mcgill.ecse223.kingdomino.model.Kingdomino;
import ca.mcgill.ecse223.kingdomino.model.Player;
import ca.mcgill.ecse223.kingdomino.model.Player.PlayerColor;
import ca.mcgill.ecse223.kingdomino.model.TerrainType;
import ca.mcgill.ecse223.kingdomino.model.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DiscardDominoStepDefinitions {

	/*
	 * Note that these step definitions and helper methods just serve as a guide to
	 * help you get started. You may change the code if required.
	 */

	@Given("the game is initialized for discard domino")
	public void the_game_is_initialized_for_discard_domino() {
		// Intialize empty game
		Kingdomino kingdomino = new Kingdomino();
		Game game = new Game(48, kingdomino);
		game.setNumberOfPlayers(4);
		kingdomino.setCurrentGame(game);
		// Populate game
		addDefaultUsersAndPlayers(game);
		createAllDominoes(game);
		game.setCurrentPlayer(game.getPlayer(0));
		KingdominoApplication.setKingdomino(kingdomino);
	}
	 

//	@Given("the player's kingdom has the following dominoes:")
//	public void the_player_s_kingdom_has_the_following_dominoes(io.cucumber.datatable.DataTable dataTable) {
//		Game game = KingdominoApplication.getKingdomino().getCurrentGame();
//		List<Map<String, String>> valueMaps = dataTable.asMaps();
//		for (Map<String, String> map : valueMaps) {
//			// Get values from cucumber table
//			Integer id = Integer.decode(map.get("id"));
//			DirectionKind dir = getDirection(map.get("dominodir"));
//			Integer posx = Integer.decode(map.get("posx"));
//			Integer posy = Integer.decode(map.get("posy"));
//
//			// Add the domino to a player's kingdom
//			Domino dominoToPlace = getdominoByID(id);
//			Kingdom kingdom = game.getPlayer(0).getKingdom();
//			DominoInKingdom domInKingdom = new DominoInKingdom(posx, posy, kingdom, dominoToPlace);
//			domInKingdom.setDirection(dir);
//			dominoToPlace.setStatus(DominoStatus.PlacedInKingdom);
//		}
//	}

	@Given("domino {int} is in the current draft")
	public void domino_is_in_the_current_draft(Integer domID) {
		Domino setInCurrentDraft = KingdominoController
				.getdominoByID(KingdominoApplication.getKingdomino().getCurrentGame(), domID);
		KingdominoApplication.getKingdomino().getCurrentGame()
				.setCurrentDraft(new Draft(DraftStatus.FaceUp, KingdominoApplication.getKingdomino().getCurrentGame()));
		KingdominoApplication.getKingdomino().getCurrentGame().getCurrentDraft().addIdSortedDomino(setInCurrentDraft);

	}

	@Given("the current player has selected domino {int}")
	public void the_current_player_has_selected_domino(Integer domID) {
		Game game = KingdominoApplication.getKingdomino().getCurrentGame();
		Player currentPlayer = game.getPlayer(0);
		Draft currentDraft = game.getCurrentDraft();
		Domino destinedDomino = KingdominoController.getdominoByID(game, domID);
		
		currentPlayer.setDominoSelection(new DominoSelection(currentPlayer, destinedDomino, currentDraft));

	}

	@Given("the player preplaces domino {int} at its initial position")
	public void the_player_preplaces_domino_at_its_initial_position(Integer domID) {
		Game game = KingdominoApplication.getKingdomino().getCurrentGame();
		Player currentPlayer = game.getPlayer(0);
		Domino destinedDomino = KingdominoController.getdominoByID(game, domID);
		
		Kingdom kingdom = currentPlayer.getKingdom();
		DominoInKingdom domInKingdom = new DominoInKingdom(0, 0, kingdom, destinedDomino);
		domInKingdom.setDirection(DirectionKind.Up);
		destinedDomino.setStatus(DominoStatus.ErroneouslyPreplaced);
	}

	@When("the player attempts to discard the selected domino")
	public void the_player_attempts_to_discard_the_selected_domino() {
		if(!KingdominoController.discardDomino(KingdominoApplication.getKingdomino().getCurrentGame().getPlayer(0)))
		{
			System.err.println("INVALID MOVE");
		}
		
	}

	@Then("domino {int} shall have status {string}")
	public void domino_shall_have_status(Integer domID, String domStatus) {
		DominoStatus actualStatus = getdominoByID(domID).getStatus();
		DominoStatus expectedStatus = getDominoStatus(domStatus);
		assertEquals(expectedStatus, actualStatus);
	}

	///////////////////////////////////////
	/// -----Private Helper Methods---- ///
	///////////////////////////////////////

	private void addDefaultUsersAndPlayers(Game game) {
		String[] userNames = { "User1", "User2", "User3", "User4" };
		for (int i = 0; i < userNames.length; i++) {
			User user = game.getKingdomino().addUser(userNames[i]);
			Player player = new Player(game);
			player.setUser(user);
			player.setColor(PlayerColor.values()[i]);
			Kingdom kingdom = new Kingdom(player);
			new Castle(0, 0, kingdom, player);
		}
	}

	private void createAllDominoes(Game game) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/main/resources/alldominoes.dat"));
			String line = "";
			String delimiters = "[:\\+()]";
			while ((line = br.readLine()) != null) {
				String[] dominoString = line.split(delimiters); // {id, leftTerrain, rightTerrain, crowns}
				int dominoId = Integer.decode(dominoString[0]);
				TerrainType leftTerrain = getTerrainType(dominoString[1]);
				TerrainType rightTerrain = getTerrainType(dominoString[2]);
				int numCrown = 0;
				if (dominoString.length > 3) {
					numCrown = Integer.decode(dominoString[3]);
				}
				new Domino(dominoId, leftTerrain, rightTerrain, numCrown, game);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new java.lang.IllegalArgumentException(
					"Error occured while trying to read alldominoes.dat: " + e.getMessage());
		}
	}

	private Domino getdominoByID(int id) {
		Game game = KingdominoApplication.getKingdomino().getCurrentGame();
		for (Domino domino : game.getAllDominos()) {
			if (domino.getId() == id) {
				return domino;
			}
		}
		throw new java.lang.IllegalArgumentException("Domino with ID " + id + " not found.");
	}

	private TerrainType getTerrainType(String terrain) {
		switch (terrain) {
		case "W":
			return TerrainType.WheatField;
		case "F":
			return TerrainType.Forest;
		case "M":
			return TerrainType.Mountain;
		case "G":
			return TerrainType.Grass;
		case "S":
			return TerrainType.Swamp;
		case "L":
			return TerrainType.Lake;
		default:
			throw new java.lang.IllegalArgumentException("Invalid terrain type: " + terrain);
		}
	}

	private DirectionKind getDirection(String dir) {
		switch (dir) {
		case "up":
			return DirectionKind.Up;
		case "down":
			return DirectionKind.Down;
		case "left":
			return DirectionKind.Left;
		case "right":
			return DirectionKind.Right;
		default:
			throw new java.lang.IllegalArgumentException("Invalid direction: " + dir);
		}
	}

	private DominoStatus getDominoStatus(String status) {
		switch (status) {
		case "inPile":
			return DominoStatus.InPile;
		case "excluded":
			return DominoStatus.Excluded;
		case "inCurrentDraft":
			return DominoStatus.InCurrentDraft;
		case "inNextDraft":
			return DominoStatus.InNextDraft;
		case "erroneouslyPreplaced":
			return DominoStatus.ErroneouslyPreplaced;
		case "correctlyPreplaced":
			return DominoStatus.CorrectlyPreplaced;
		case "placedInKingdom":
			return DominoStatus.PlacedInKingdom;
		case "discarded":
			return DominoStatus.Discarded;
		default:
			throw new java.lang.IllegalArgumentException("Invalid domino status: " + status);
		}
	}
}

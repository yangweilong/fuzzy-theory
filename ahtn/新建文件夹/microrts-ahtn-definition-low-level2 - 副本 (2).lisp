(defdomain microrts-low-level
	(

		;; ---- ---- ---- ---- ---- 
		;; ---- OPERATORS	
		;; ---- ---- ---- ---- ---- 

		(:operator (!wait-for-free-unit ?player ?essensial)
			(true)
		)
		(:operator (!fill-with-idles ?player ?essensial)
			(true)
		)
		(:operator (!idle ?unitid ?essensial)
			(unit ?unitid ?_ ?_ ?_ ?_)
		)
		(:operator (!move-into-attack-range ?unitid1 ?unitid2 ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!move-into-harvest-range ?unitid1 ?unitid2 ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!move-into-return-range ?unitid1 ?unitid2 ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!attack ?unitid1 ?unitid2 ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-attack ?type1)
				(in-attack-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!harvest ?unitid1 ?unitid2 ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 Resource ?_ ?r2 ?oldposition2)
				(can-harvest ?type1)
				(in-harvest-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!return ?unitid1 ?unitid2 ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 1 ?oldposition1)
				(unit ?unitid2 Base ?_ ?r2 ?oldposition2)
				(can-harvest ?type1)
				(in-return-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!produce ?unitid1 ?direction ?type ?essensial)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(can-produce ?type1 ?type)
				(has-resources-to-produce ?player1 ?type)
				(free-building-position (neighbor-position ?oldposition1 ?direction))
			)	
		)


		;; ---- ---- ---- ---- ---- 
		;; ---- METHODS	
		;; ---- ---- ---- ---- ---- 
		;; fully destroy the enemy: one method for each possible action that can be executed (to allow for the full search tree)
		(:method dp-1 (destroy-player ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?_ ?_ ?player1 ?_ ?_)
							))
				(:method (destroy-player-internal ?player1 ?player2 -1))
			)
		)

		(:method dpi-nextunit (destroy-player-internal ?player1 ?player2 ?lastunit)
			(:sequence
				(:condition (next-available-unit ?lastunit ?player1 ?unit))
				(:parallel
					(:method (unit-order ?player1 ?player2 ?unit))
				)
			)
		)

		(:method dpi-nomoreunits (destroy-player-internal ?player1 ?player2 ?lastunit)
			(:sequence
				(:condition (no-more-available-units ?lastunit ?player1))
				(:operator (!fill-with-idles ?player1 0))
				(:operator (!wait-for-free-unit ?player1 0))
			)
		)


		(:method uo-attack (unit-order ?player1 ?player2 ?unitid1)
			(:sequence 
			  (:condition (and
			  				(unit ?unitid1 ?type1 ?player1 ?_ ?_)
			  				(unit ?unitid2 ?type2 ?player2 ?_ ?_)
							(can-attack ?type1)
			  				))
			  (:operator (!move-into-attack-range ?unitid1 ?unitid2 0))
			  (:condition (in-attack-range ?unitid1 ?unitid2 0))
			  (:operator (!attack ?unitid1 ?unitid2 0))
			)
		)
		(:method uo-harvest (unit-order ?player1 ?player2 ?unitid)
			(:sequence 
				(:condition (and 
								(unit ?unitid Worker ?player1 ?_ ?_)
								(unit ?resourceid Resource ?_ ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
							))
				(:operator (!move-into-harvest-range ?unitid ?resourceid 0))
				(:operator (!harvest ?unitid ?resourceid 0))
				(:operator (!move-into-return-range ?unitid ?baseid 0))
				(:operator (!return ?unitid ?baseid 0))
			)
		)
		(:method uo-produce (unit-order ?player1 ?player2 ?unitid1)
			(:sequence
				(:condition (and 
								(unit ?unitid1 ?type1 ?player1 ?_ ?_)
								(can-produce ?type1 ?type)
								(has-resources-to-produce ?player1 ?type)
								(free-producing-direction ?unitid1 ?direction)
							))
				(:operator (!produce ?unitid1 ?direction ?type 0))
			)
		)
		(:method uo-idle (unit-order ?player1 ?player2 ?unitid1)
			(:sequence
				(:condition (and 
								(unit ?unitid1 ?type1 ?player1 ?_ ?_)
							))
				(:operator (!idle ?unitid1 0))
			)
		)
	)
)

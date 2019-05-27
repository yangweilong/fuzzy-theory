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
							)
				)
				(:phase (phase-dp-1-phase ?player1 ?player2))
			)
		)

		(:method dpi-nextunit (destroy-player-internal ?player1 ?player2 ?lastunit)
			(:sequence
				(:condition (next-available-unit ?lastunit ?player1 ?unit))
				(:parallel
					(:phase (phase-dpi-nextunit-phase ?player1 ?player2))
				)
			)
		)

		(:method dpi-nomoreunits (destroy-player-internal ?player1 ?player2 ?lastunit)
			(:sequence
				(:condition (no-more-available-units ?lastunit ?player1))
				(:phase (phase-dpi-nomoreunits-phase ?player1))
			)
		)


		(:method uo-attack (unit-order ?player1 ?player2 ?unitid1 ?essential)
			(:sequence 
			  (:condition (and
			  				(unit ?unitid1 ?type1 ?player1 ?_ ?_)
			  				(unit ?unitid2 ?type2 ?player2 ?_ ?_)
							(can-attack ?type1)
			  				))
			   (:phase (phase-uo-attack-phase ?unitid1 ?unitid2))
			)
		)
		(:method uo-harvest (unit-order ?player1 ?player2 ?unitid ?essential)
			(:sequence 
				(:condition (and 
								(unit ?unitid Worker ?player1 ?_ ?_)
								(unit ?resourceid Resource ?_ ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
							))
				(:phase (phase-uo-harvest-phase ?player1 ?player2 ?unitid))
			)
		)
		(:method uo-produce (unit-order ?player1 ?player2 ?unitid1 ?essential)
			(:sequence
				(:condition (and 
								(unit ?unitid1 ?type1 ?player1 ?_ ?_)
								(can-produce ?type1 ?type)
								(has-resources-to-produce ?player1 ?type)
								(free-producing-direction ?unitid1 ?direction)
							))
				(:phase (phase-uo-produce-phase ?player1 ?player2 ?unitid1))
			)
		)
		(:method uo-idle (unit-order ?player1 ?player2 ?unitid1 ?essential)
			(:sequence
				(:condition (and 
								(unit ?unitid1 ?type1 ?player1 ?_ ?_)
							))
				(:phase (phase-uo-idle-phase ?unitid1))
			)
		)
		
		;;------phase-------
		(:phase phase-dp-1-phase (dp-1 ?player1 ?player2)
		  (:sequence
			 (:parallel
                (:method (destroy-player-internal ?player1 ?player2 -1))  ;;?essensial
              )				
		  )
		)
		
		(:phase phase-dpi-nextunit-phase (dpi-nextunit ?player1 ?player2)
		  (:sequence
			 (:parallel
                (:method (unit-order ?player1 ?player2 ?unit 0))  ;;?essensial
              )				
		  )
		)
		
		(:phase phase-dpi-nomoreunits-phase (dpi-nomoreunits ?player1)
		  (:sequence
			 (:parallel
                (:operator (!fill-with-idles ?player1 0))
				(:operator (!wait-for-free-unit ?player1 0))
              )				
		  )
		)
		
		(:phase phase-uo-attack-phase (uo-attack ?unitid1 ?unitid2)
		  (:sequence
			 (:parallel
                (:operator (!move-into-attack-range ?unitid1 ?unitid2 0))
			    (:operator (!attack ?unitid1 ?unitid2 0))
              )				
		  )
		)
		
		(:phase phase-uo-harvest-phase (uo-harvest ?player1 ?player2 ?unitid)
		  (:sequence
			 (:parallel
                (:operator (!move-into-harvest-range ?unitid ?resourceid 0))
				(:operator (!harvest ?unitid ?resourceid 0))
				(:operator (!move-into-return-range ?unitid ?baseid 0))
				(:operator (!return ?unitid ?baseid 0))
              )				
		  )
		)
		
		(:phase phase-uo-produce-phase (uo-produce ?player1 ?player2)
		  (:sequence
			 (:parallel
                (:operator (!produce ?unitid1 ?direction ?type 0))
              )				
		  )
		)
		
		(:phase phase-uo-idle-phase (uo-idle ?player1 ?player2 ?unitid1)
		  (:sequence
			 (:parallel
                 (:operator (!idle ?unitid1 0))               
              )				
		  )
		)
	)
)

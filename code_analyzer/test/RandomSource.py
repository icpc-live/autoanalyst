#!/usr/bin/python
# Generate random source files based on problem names

import random

class RandomSource:
    def __init__( self, dbConn ):
        # List of source file extensions.
        self.extensionList = [ 'cc', 'cpp', 'c', 'java' ]
        
        # List of plausible directory names, just so we have to look around
        # for the source files.
        self.dirNameList = [ 'work', 'problems', 'soln', 'dev' ]
        
        # Random characters to use in file contents.
        self.fileCharacters = """abcdefghijklmnopqrstuvwxyz


ABCDEFGHIJKLMNOPQRSTUVWXYZ"{}-()[];"""

        # map from problem ID to a list of expected file name components
        self.probNames = {}

        
        # Read the list of problem names
        cursor = dbConn.cursor()
        cursor.execute( "SELECT problem_id, name FROM problem_name" )
        row = cursor.fetchone()
        while ( row != None ):
            if ( row[ 0 ] in self.probNames ):
                self.probNames[ row[ 0 ] ].append( row[ 1 ] )
            else:
                self.probNames[ row[ 0 ] ] = [ row[ 1 ] ]

            row = cursor.fetchone()

        cursor.close()

    def writeSourceFile( self, path ):
        """Given a path, fill it with random contents."""
        output = open( path, "w" )

        len = random.randrange( 100, 2000 )
        
        # Print headers for all the problems.
        for i in range( len ):
            output.write( random.choice( self.fileCharacters ) )
        output.write( "\n" )

        output.close()

    def appendSourceFile( self, path ):
        """Append more random content to the given path."""
        output = open( path, "a" )

        len = random.randrange( 10, 200 )
        
        # Print headers for all the problems.
        for i in range( len ):
            output.write( random.choice( self.fileCharacters ) )
        output.write( "\n" )

        output.close()

    def chooseRandomSourceFile( self ):
        """Generate a source file for a random problem, returning a 3-tuple with
           the local path to the source file's directory, the name of the source file
           and the chosen problem id."""
        problem = random.choice( self.probNames.keys() )
        pname = random.choice( self.probNames[ problem ] )
        extension = random.choice( self.extensionList )
                
        # make a random path.
        depth = random.randrange( 0, 2 )
        path = ""
        for j in range( depth ):
            path = "%s%s/" % ( path, random.choice( self.dirNameList ) )
            
        style = random.choice( [ 'problem-a.c',
                                 'problem_A.c',
                                 'a/solution.c',
                                 'A/code/soln.c',
                                 'a_latest.c',
                                 'sol-a.c' ] )
        
        fileName = ""
        if style == 'problem-a.c':
            fileName = "problem-%s.%s" % ( pname.lower(), extension )
        elif style == 'problem_A.c':
            fileName = "problem_%s.%s" % ( pname.capitalize(), extension )
        elif style == 'a/solution.c':
            path = "%s%s/" % ( path, pname.lower() )
            fileName = "solution.%s" % ( extension )
        elif style == 'A/code/soln.c':
            path = '%s%s/code/' % ( path, pname.capitalize() )
            fileName = "soln.%s" % ( extension )
        elif style == 'a_latest.c':
            fileName = "%s_latest.%s" % ( pname.lower(), extension )
        elif style == 'sol-a.c':
            fileName = "sol-%s.%s" % ( pname.lower(), extension )
        else:
            print "Error, bad file naem style: %s" % style

        # Make the directories, then add on the filename.
        return ( path, fileName, problem )

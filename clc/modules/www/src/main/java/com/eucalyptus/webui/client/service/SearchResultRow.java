package com.eucalyptus.webui.client.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gwt.view.client.ProvidesKey;

public class SearchResultRow implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  public static final ProvidesKey<SearchResultRow> KEY_PROVIDER = new ProvidesKey<SearchResultRow>( ) {

    @Override
    public Object getKey( SearchResultRow item ) {
      if ( item != null ) {
        return item.getField( 0 );
      }
      return null;
    }
    
  };
  
  private ArrayList<String> row = new ArrayList<String>( );
  private ArrayList<String> link = new ArrayList<String>( );
  
  // Row specific extra field descriptions
  private ArrayList<SearchResultFieldDesc> extraFields = new ArrayList<SearchResultFieldDesc>( );
  
  public SearchResultRow copy() {
	  return new SearchResultRow(row, extraFields, link);
  }
  
  public SearchResultRow( ) {
  }
  
  public SearchResultRow( List<String> row ) {
    this( row, null, null );
  }
  
  public SearchResultRow( List<String> row, List<String> link ) {
	    this( row, null, link );
  }
  
	public SearchResultRow(List<String> row, List<SearchResultFieldDesc> extra, List<String> link) {
		if (row != null) {
			for (String field : row) {
				if (field == null) {
					field = "";
				}
				this.row.add(field);
			}
		}
		if (extra != null) {
			for (SearchResultFieldDesc field : extra) {
				if (field == null) {
					field = new SearchResultFieldDesc();
				}
				this.extraFields.add(field);
			}
		}
		if (link != null) {
			for (String field : link) {
				if (field == null) {
					field = "";
				}
				this.link.add(field);
			}
		}
	}
  
  @Override
  public String toString( ) {
    StringBuilder sb = new StringBuilder( );
    sb.append( "(" );
    sb.append( "extraFields=" ).append( extraFields ).append( "," );
    sb.append( "rows=" ).append( row );
    sb.append( ")" );
    return sb.toString( );
  }
  
  public String getField( int i ) {
    if ( i < row.size( ) ) {
      return row.get( i );
    }
    return null;
  }
  
  public void setField( int i, String val ) {
    if ( val == null ) {
      val = "";
    }    
    row.set( i, val );
  }

  public void addField( String val ) {
    if ( val == null ) {
      val = "";
    }
    row.add( val );
  }
  
  public String getLink( int i ) {
	if ( i < link.size( ) ) {
	  return link.get( i );
	}
	return null;
  }
  
  public void setLink( int i, String val ) {
	if ( val == null ) {
	  val = "";
	}    
	link.set(i, val);
  }

  public void addLink( String val ) {
	if ( val == null ) {
		val = "";
	}
	link.add( val );
  }
  
  public void setExtraFieldDesc( int i, SearchResultFieldDesc desc ) {
    this.extraFields.set( i, desc );
  }

  public void addExtraFieldDesc( SearchResultFieldDesc desc ) {
    this.extraFields.add( desc );
  }
  
  public SearchResultFieldDesc getExtraFieldDesc( int i ) {
    return this.extraFields.get( i );
  }
  
  public void setExtraFieldDescs( List<SearchResultFieldDesc> descs ) {
    this.extraFields.addAll( descs );
  }
  
  public ArrayList<SearchResultFieldDesc> getExtraFieldDescs( ) {
    return this.extraFields;
  }
  
  public ArrayList<String> getRow( ) {
    return this.row;
  }
  
  public ArrayList<String> getLink( ) {
	    return this.link;
  }
  
  @Override
  public boolean equals( Object that ) {
    if ( this == that ) {
      return true;
    }
    if ( ! ( that instanceof SearchResultRow ) ) {
      return false;
    }
    SearchResultRow thatRow = ( SearchResultRow ) that;
    if ( this.row.size( ) != thatRow.row.size( ) ) {
      return false;
    }
    for ( int i = 0; i < this.row.size( ); i++ ) {
      if ( ! ( this.row.get( i ).equals( thatRow.row.get( i ) ) ) ) {
        return false;
      }
    }
    
    if ( this.link.size( ) != thatRow.link.size( ) ) {
        return false;
    }
    for ( int i = 0; i < this.link.size( ); i++ ) {
        if ( ! ( this.link.get( i ).equals( thatRow.link.get( i ) ) ) ) {
          return false;
        }
    }
    return true;
  }
}

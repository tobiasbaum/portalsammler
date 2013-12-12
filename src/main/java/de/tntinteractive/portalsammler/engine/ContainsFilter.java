/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of Portalsammler.

    Portalsammler is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Portalsammler is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Portalsammler.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tntinteractive.portalsammler.engine;

public final class ContainsFilter extends DocumentFilter {

    private final String filterString;

    public ContainsFilter(final String filterString) {
        this.filterString = filterString.toLowerCase();
    }

    @Override
    public boolean shallShow(final DocumentInfo s) {
        return s.getKeywords().toLowerCase().contains(this.filterString)
            || s.getSourceId().toLowerCase().contains(this.filterString);
    }

}

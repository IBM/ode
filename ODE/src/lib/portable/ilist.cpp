#define _ODE_LIB_PORTABLE_ILIST_CPP_
#include "lib/portable/ilist.hpp"


boolean ODEDListBase::insertBeforeElement( ODEDLink *existing_element,
    ODEDLink *new_element )
{
  if (existing_element != 0)
  {
    if (existing_element != head_)  //existing_element is not head
    {
      new_element->prev_ = existing_element->prev_;
      existing_element->prev_->next_ = new_element;
    }
    else
    {
      new_element->prev_ = 0;
      head_ = new_element;
    }

    new_element->next_ = existing_element;
    existing_element->prev_ = new_element;

    ++nitems_;
    return (true);
  }

  //if the list is empty
  if (isEmpty())
  {
    new_element->prev_ = 0;
    new_element->next_ = 0;
    head_ = new_element;
    tail_ = new_element;
    nitems_++;
    return (true);
  } 
    //if existing_element=0 then
    //insert new_element at the end and make it tail
 
  tail_->next_ = new_element;
  new_element->prev_ = tail_;
  new_element->next_ = 0;
  tail_ = new_element;
  nitems_++;
  return (true);

}

/* *********************************************************
*
*  removeAll(): Destroys all the nodes.
*  Just unlink the nodes and reset everything.
*
* **********************************************************
*/
void ODEDListBase::removeAll()
{
  ODEDLink* ret=0;
  while (head_ != tail_)
  {
    ret = head_->next_;
    delete head_;
    head_=ret;
  }
  delete head_;
  init();
}


boolean ODEDListBase::removeElement( ODEDLink *element )
{
  if (element != 0)
  {
    if (head_ != tail_)
    {
      if (element != head_)
      {
        if (element != tail_)
        {
          element->prev_->next_ = element->next_;
          element->next_->prev_ = element->prev_;
        }
        else                          //if element is tail
        {
          tail_ = element->prev_;
          tail_->next_ = 0;
        }
      }
      else                           //if element is head
      {
        head_ = element->next_;
        head_->prev_ = 0;
      }

      delete element;
      --nitems_;
      return (true);
    }

    //if there is only one element in the list, delete it.
    delete element;
    init();
    return (true);
  }
  return (false);
}

/* *********************************************************
*
*  findPositionAt(unsigned long a): Find element at given 
*  position.
* 
* **********************************************************
*/
ODEDLink* ODEDListBase::findPositionAt(unsigned long i) const
{
  if (isEmpty() || i > lastIndex())
    return 0;

  ODEDLink* index;
  unsigned long count;

  if (i < (lastIndex() - i))
  {
    //Search forward from head
    count=1;
    index = head_;
    while(index != tail_)
    {
      if (count++ == i) 
        return index;
      index = index->next_;
    }
    if (count == i)
      return index;
  }
  else
  {
    //Search backwards from tail
    index = tail_;
    count=nitems_;
    while(index != head_)
    {
      if (count-- == i) 
        return index;
      index = index->prev_;
    }
    if (count == i)
      return index;
  }
  return 0;
}
